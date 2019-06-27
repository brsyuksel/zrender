package zrender.browser.interpreters

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import fs2.{Stream, Pipe}
import spinoco.fs2.http.websocket.Frame

import org.scalatest._

import zrender.client.{WebSocketClient, Endpoint}

class ChromeBrowserSpec extends FlatSpec with Matchers {
  class MockClient(inbound: Stream[Task, Frame[String]])
      extends WebSocketClient[Task, Pipe[Task, Frame[String], Frame[String]]] {
    def pipe(endpoint: Endpoint, payload: Pipe[Task, Frame[String], Frame[String]]): Task[Unit] =
      inbound.through(payload).compile.drain
  }

  def stream(s: String): Stream[Task, Frame[String]] =
    Stream(s).map(Frame.Text(_)).repeat.covary[Task]

  def mockCb: MockClient => ChromeBrowser = new ChromeBrowser(Endpoint.ws("", 0, "/"))(_)

  val rt = new DefaultRuntime {}

  ".createCtx" should "return ctxID" in {
    val js = """{"id": 1, "result": {"browserContextId": "ctxId"}}"""
    val mock = new MockClient(stream(js))
    val cb = mockCb(mock)

    val ctxId = rt.unsafeRun(cb.createCtx)
    assert(ctxId == "ctxId")
  }

  ".disposeCtx" should "return unit" in {
    val js = """{"id": 1, "result": {}}"""
    val mock = new MockClient(stream(js))
    val cb = mockCb(mock)

    val r = rt.unsafeRun(cb.disposeCtx("ctxId"))
    r shouldBe ()
  }

  ".createTarget" should "return tID" in {
    val js = """{"id": 1, "result": {"targetId": "tId"}}"""
    val mock = new MockClient(stream(js))
    val cb = mockCb(mock)

    val tId = rt.unsafeRun(cb.createTarget("ctxId"))
    assert(tId == "tId")
  }

  ".closeTarget" should "return unit" in {
    val js = """{"id": 1, "result": {}}"""
    val mock = new MockClient(stream(js))
    val cb = mockCb(mock)

    val r = rt.unsafeRun(cb.closeTarget("ctxId"))
    r shouldBe ()
  }

}
