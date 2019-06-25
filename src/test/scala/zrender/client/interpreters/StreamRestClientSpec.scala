package zrender.client.interpreters

import scala.concurrent.duration._

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import fs2.{Stream, Pipe}
import spinoco.fs2.http.{HttpClient, HttpRequest, HttpResponse}
import spinoco.fs2.http.sse.{SSEDecoder}
import spinoco.fs2.http.websocket.{WebSocketRequest, Frame}
import spinoco.protocol.http.{HttpResponseHeader, HttpStatusCode}
import scodec.{Decoder => SDecoder, Encoder => SEncoder}
import io.circe.Decoder

import org.scalatest._
import org.scalatest.prop.PropertyChecks

import zrender.client.{JsDecoder, Endpoint}

class StreamRestClientSpec extends FlatSpec with PropertyChecks {
  class MockHttpClient(r: Stream[Task, HttpResponse[Task]]) extends HttpClient[Task] {
    def sse[A: SSEDecoder](
      request: HttpRequest[Task],
      maxResponseHeaderSize: Int,
      chunkSize: Int): Stream[Task, A] = ???

    def websocket[I: SDecoder, O: SEncoder](
      request: WebSocketRequest,
      pipe: Pipe[Task, Frame[I], Frame[O]],
      maxResponseHeaderSize: Int,
      chunkSize: Int,
      maxFrameSize: Int): Stream[Task, Option[HttpResponseHeader]] = ???

    def request(
      request: HttpRequest[Task],
      chunkSize: Int = 32 * 1024,
      maxResponseHeaderSize: Int = 4096,
      timeout: Duration = 5 seconds
    ): Stream[Task, HttpResponse[Task]] = r
  }

  def resp(b: String): Stream[Task, HttpResponse[Task]] =
    Stream.eval(Task(HttpResponse[Task](
      HttpResponseHeader(HttpStatusCode.Ok, ""),
      Stream.fromIterator[Task, Byte](b.getBytes.toIterator)
    )))

  case class RespData(a: Int, b: String, c: List[String])
  object RespData {
    implicit val dec: Decoder[RespData] = Decoder.forProduct3("a", "b", "c")(RespData.apply)

    implicit val jsDec: JsDecoder[RespData] = new JsDecoder[RespData] {
      def decoder[B]: B = dec.asInstanceOf[B]
    }
  }

  implicit val rt = new DefaultRuntime {}

  "StreamRestClient.get" should "return decoded case class" in {
    val exp = RespData(1, "test", List("abc", "def"))
    val response = resp("""{"a": 1, "b": "test", "c": ["abc", "def"]}""")
    val client = new MockHttpClient(response)

    val s = new StreamRestClient(client)
    val res = rt.unsafeRun(s.get[RespData](Endpoint.http("host", "/")))
    assert(res === exp)
  }
}
