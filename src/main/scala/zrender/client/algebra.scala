package zrender.client

trait RestClient[F[_]] {
  def get[A : JsDecoder](endpoint: Endpoint): F[A]
}

trait WebSocketClient[F[_], P] {
  def pipe(endpoint: Endpoint, payload: P): F[Unit]
}
