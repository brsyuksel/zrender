package zrender.browser.interpreters

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._

import fs2.{Stream, Pipe}
import spinoco.fs2.http.websocket.Frame

import zrender.client.{Endpoint, WebSocketClient}

class MockClient(inbound: Stream[Task, Frame[String]])
    extends WebSocketClient[Task, Pipe[Task, Frame[String], Frame[String]]] {
  def pipe(endpoint: Endpoint, payload: Pipe[Task, Frame[String], Frame[String]]): Task[Unit] =
    inbound.through(payload).compile.drain
}
