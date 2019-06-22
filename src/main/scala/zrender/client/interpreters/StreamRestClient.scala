package zrender.client.interpreters

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import cats.effect.ConcurrentEffect
import spinoco.fs2.http.{HttpClient, HttpRequest}
import io.circe.Decoder
import io.circe.parser._
import io.circe.fs2._

import zrender.client._
import zrender.client.interpreters.implicits._

final class StreamRestClient[F[_]: ConcurrentEffect](C: HttpClient[F]) extends RestClient[F] {
  private def createRequest(endpoint: Endpoint): HttpRequest[F] =
    HttpRequest.get[F](endpoint.httpx)

  def get[A: JsDecoder](endpoint: Endpoint): F[A] = {
    implicit val dec: Decoder[A] = JsDecoder[A].decoder[Decoder[A]]
    C.request(createRequest(endpoint)).flatMap { resp =>
      resp.body.through(byteArrayParser).through(decoder)
    }.compile.lastOrError
  }
}
