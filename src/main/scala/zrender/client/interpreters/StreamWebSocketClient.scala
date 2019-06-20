package zrender.client.interpreters

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import cats.effect.ConcurrentEffect
import fs2.{Stream, Pipe}
import spinoco.fs2.http.HttpClient
import spinoco.fs2.http.websocket.Frame
import scodec.Codec
import scodec.codecs.utf8

import zrender.client._
import zrender.client.interpreters.implicits._

final class StreamWebSocketClient[F[_]: ConcurrentEffect](C: HttpClient[F])
    extends WebSocketClient[F, Pipe[F, Frame[String], Frame[String]]] {

  implicit val c: Codec[String] = utf8

  def pipe(endpoint: Endpoint, payload: Pipe[F, Frame[String], Frame[String]]): F[Unit] =
    C.websocket(endpoint.ws, payload).compile.drain
}
