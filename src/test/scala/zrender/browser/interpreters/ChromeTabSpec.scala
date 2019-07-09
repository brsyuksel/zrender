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

class ChromeTabSpec extends FlatSpec with Matchers with PropertyChecks {
  def stream(s: String *): Stream[Task, Frame[String]] =
    Stream(s: _*).map(Frame.Text(_)).repeat.covary[Task]

  def mockCt(l: Option[List[String]], reqH: Boolean, resH: Boolean, reqInt: Double): MockClient => ChromeTab =
    new ChromeTab(Endpoint.ws("", 0, "/"), "test", ZRender(l, reqH, resH, reqInt))(_)

  val rt = new DefaultRuntime {}

  ".init" should "return unit" in {
    forAll { (l: Option[List[String]], b: Boolean) =>
      val i = 7 + (if(l.isEmpty) 0 else 1) + (if(b) 1 else 0)
      val inbound = stream("""{"id": 1, "result":{}}""", s"""{"id": $i, "result": {}}""")
      val mock = new MockClient(inbound)
      val ct = mockCt(l, b, false, 0.5)(mock)

      rt.unsafeRun(ct.init) shouldBe ()
    }
  }

  ".navigate" should "return unit after requests completed" in {
    val f: Double => String =
      d => s"""{"method": "Network.loadingFinished", "params": {"timestamp": $d}}"""

    val inbound = stream(f(0.1), f(0.2), f(0.3), f(0.4), f(1.0))
    val mock = new MockClient(inbound)
    val ct = mockCt(none, false, false, 0.5)(mock)

    rt.unsafeRun(ct.navigate("")) shouldBe ()
  }

  ".source" should "return page source" in {
    val inbound = stream(s"""{"id": 1, "result": {"result": {"value": "source"}}}""")
    val mock = new MockClient(inbound)
    val ct = mockCt(none, false, false, 0.5)(mock)

    rt.unsafeRun(ct.source) shouldBe "source"
  }
}
