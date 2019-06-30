package zrender.browser.interpreters

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import fs2.{Pipe, Stream}
import spinoco.fs2.http.websocket.Frame
import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.circe.optics.JsonPath

import zrender.browser.Browser
import zrender.client.{Endpoint, WebSocketClient}
import zrender.browser.devtools.Message

final class ChromeBrowser(endpoint: Endpoint)(
  C: WebSocketClient[Task, Pipe[Task, Frame[String], Frame[String]]]
) extends Browser[Task] {

  type P[F[_]] = Pipe[F, Frame[String], Frame[String]]
  type RP[F[_], A] = Ref[A] => P[F]

  private def ctxPipe: RP[Task, String] = ref => inbound => {
    val out = Stream(Message.createCtx(1))
      .map(m => Frame.Text(m.asJson.noSpaces))
      .covary[Task]

    val in = inbound
      .map(f => parse(f.a).getOrElse(Json.Null))
      .map(j => JsonPath.root.result.browserContextId.string.getOption(j))
      .filter(_.nonEmpty)
      .evalMap(o => ref.set(o.get))
      .map(_ => Frame.Text(""))

    (out merge in).take(2)
  }

  private def ctxDisposePipe(ctxId: String): P[Task] = inbound => {
    val out = Stream(Message.disposeCtx(ctxId)(1))
      .map(m => Frame.Text(m.asJson.noSpaces))
      .covary[Task]

    (out merge inbound).take(2)
  }

  private def targetCreate(ctxId: String): RP[Task, String] = ref => inbound => {
    val out = Stream(Message.createTarget(ctxId)(1))
      .map(m => Frame.Text(m.asJson.noSpaces))
      .covary[Task]

    val in = inbound
      .map(f => parse(f.a).getOrElse(Json.Null))
      .map(j => JsonPath.root.result.targetId.string.getOption(j))
      .filter(_.nonEmpty)
      .evalMap(o => ref.set(o.get))
      .map(_ => Frame.Text(""))

    (out merge in).take(2)
  }

  private def targetClose(tId: String): P[Task] = inbound => {
    val out = Stream(Message.closeTarget(tId)(1))
      .map(m => Frame.Text(m.asJson.noSpaces))
      .covary[Task]

    (out merge inbound).take(2)
  }

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
