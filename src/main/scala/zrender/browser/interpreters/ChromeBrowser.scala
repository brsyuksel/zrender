package zrender.browser.interpreters

import scalaz._
import Scalaz._
import scalaz.zio._
import fs2.{Pipe, Stream}
import spinoco.fs2.http.websocket.Frame

import zrender.browser.Browser
import zrender.client.{Endpoint, WebSocketClient}

final class ChromeBrowser(endpoint: Endpoint)(
  C: WebSocketClient[Task, Pipe[Task, Frame[String], Frame[String]]]
) extends Browser[Task] {

  type P[F[_]] = Pipe[F, Frame[String], Frame[String]]
  type RP[F[_], A] = Ref[A] => P[F]

  private def ctxPipe: RP[Task, String] = ???
  private def ctxDisposePipe(ctxId: String): P[Task] = ???
  private def targetCreate(ctxId: String): RP[Task, String] = ???
  private def targetClose(tId: String): P[Task] = ???

  def createCtx: Task[String] = for {
    ref <- Ref.make[String]("")
    _ <- C.pipe(endpoint, ctxPipe(ref))
    v <- ref.get
  } yield v

  def disposeCtx(ctxId: String): Task[Unit] =
    C.pipe(endpoint, ctxDisposePipe(ctxId))

  def createTarget(ctxId: String): Task[String] = for {
    ref <- Ref.make[String]("")
    _ <- C.pipe(endpoint, targetCreate(ctxId)(ref))
    v <- ref.get
  } yield v

  def closeTarget(tId: String): Task[Unit] =
    C.pipe(endpoint, targetClose(tId))
}
