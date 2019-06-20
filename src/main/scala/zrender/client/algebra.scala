package zrender.client

trait RestClient[F[_]] {
  def get[A](endpoint: Endpoint): F[A]
}

trait WebSocketClient[F[_]] {
  def sendRecieve[A, P](endpoint: Endpoint, payload: P): F[A]
}
