package zrender.client

trait JsDecoder[A] {
  def decoder[F[_]]: F[A]
}
object JsDecoder {
  def apply[A: JsDecoder]: JsDecoder[A] = implicitly[JsDecoder[A]]
}

trait RestClient[F[_]] {
  def get[A: JsDecoder](endpoint: Endpoint): F[A]
}

trait WebSocketClient[F[_], P] {
  def pipe(endpoint: Endpoint, payload: P): F[Unit]
}
