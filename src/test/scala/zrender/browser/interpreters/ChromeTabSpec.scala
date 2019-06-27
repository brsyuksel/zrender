package zrender.browser.interpreters

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import fs2.Stream
import spinoco.fs2.http.websocket.Frame

import org.scalatest._
import org.scalatest.prop.PropertyChecks
import org.scalacheck._

import zrender.client.Endpoint
import zrender.conf.ZRender

class ChromeTabSpec extends FlatSpec with PropertyChecks {
  def stream(s: String *): Stream[Task, Frame[String]] =
    Stream(s: _*).map(Frame.Text(_)).repeat.covary[Task]

  def mockCt(l: Option[List[String]], reqH: Boolean, resH: Boolean): MockClient => ChromeTab =
    new ChromeTab(Endpoint.ws("", 0, "/"), "test", ZRender(l, reqH, resH))(_)

  val rt = new DefaultRuntime {}

  ".navigate" should "return source string" in {
    forAll { (l: Option[List[String]], b: Boolean) =>
      val i = 9 + (if(l.isEmpty) 0 else 1) + (if(b) 1 else 0)
      val inbound = stream(s"""{"id": $i, "result": {}}""",
        s"""{"id": 1, "result": {"result": {"value": "source"}}}""")
      val mock = new MockClient(inbound)
      val ct = mockCt(l, b, false)(mock)

      assert(rt.unsafeRun(ct.navigate("")) == "source")
    }
  }
}
