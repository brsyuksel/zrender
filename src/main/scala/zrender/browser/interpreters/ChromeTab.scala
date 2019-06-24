package zrender.browser.interpreters

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import fs2.{Stream, Pipe}
import spinoco.fs2.http.websocket.Frame
import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.circe.optics.JsonPath

import zrender.conf.ZRender
import zrender.browser.Tab
import zrender.client.{Endpoint, WebSocketClient}
import zrender.browser.devtools.Message


final class ChromeTab(endpoint: Endpoint, userAgent: String, conf: ZRender)(
  C: WebSocketClient[Task, Pipe[Task, Frame[String], Frame[String]]]
) extends Tab[Task] {

  type P[F[_]] = Pipe[F, Frame[String], Frame[String]]
  type RP[F[_], A] = Ref[A] => P[F]

  private def m2Str[A: Encoder](fn: Int => Message[A]): Int => String = fn map { _.asJson.noSpaces }

  private def messages(url: String): List[String] = List(
    Message.enableDOM.some.map(m2Str(_)),
    Message.enablePage.some.map(m2Str(_)),
    Message.enableSecurity.some.map(m2Str(_)),
    Message.enableNetwork.some.map(m2Str(_)),
    Message.enableRuntime.some.map(m2Str(_)),
    Message.ignoreCertErrs.some.map(m2Str(_)),
    Message.userAgent(userAgent).some.map(m2Str(_)),
    Message.bypassWorkers.some.map(m2Str(_)),
    conf.reqHeader.some.filter(_ === true).map(_ => Message.extraHeaders(Map("X-ZRender" -> "1.0.0"))).map(m2Str(_)),
    conf.blockedUrls.map(Message.blockUrls).map(m2Str(_)),
    Message.navigate(url).some.map(m2Str(_)))
    .flatten
    .zipWithIndex
    .map {case (fn, l) => fn(l.toInt + 1)}

  private def navigatePipe(url: String): P[Task] = {
    val m = messages(url)
    val lastIndex = m.length

    inbound => {
      val out = Stream.fromIterator[Task, String](m.toIterator).map(Frame.Text(_))
      val in = inbound
        .map(f => parse(f.a).getOrElse(Json.Null))
        .map(j => JsonPath.root.id.int.getOption(j))
        .filter(_.nonEmpty)
        .filter(_ === lastIndex.some)
        .take(1)

      out concurrently in
    }
  }

  private def sourcePipe: RP[Task, String] = ref => inbound => {
    val out = Stream(Message.evaluate("document.firstElementChild.outerHTML")(1))
      .map(m => Frame.Text(m.asJson.noSpaces))
      .covary[Task]

    val in = inbound
      .map(f => parse(f.a).getOrElse(Json.Null))
      .map(j => JsonPath.root.result.result.value.string.getOption(j))
      .filter(_.nonEmpty)
      .take(1)
      .evalMap(o => ref.set(o.get))

    out concurrently in
  }

  def navigate(url: String): Task[String] = for {
    _ <- C.pipe(endpoint, navigatePipe(url))
    ref <- Ref.make[String]("")
    _ <- C.pipe(endpoint, sourcePipe(ref))
    v <- ref.get
  } yield v
}