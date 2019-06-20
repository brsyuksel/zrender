package zrender.client

case class Endpoint(scheme: Endpoint.Scheme, host: String, port: Int, path: String) {
  def uri: String = s"$scheme://$host:$port$path"
}
object Endpoint {
  trait Scheme
  object Scheme {
    case object WS extends Scheme { override def toString: String = "ws" }
    case object HTTP extends Scheme { override def toString: String = "http" }
    case object HTTPS extends Scheme { override def toString: String = "https" }
  }

  def ws(host: String, port: Int, path: String): Endpoint =
    Endpoint(Scheme.WS, host, port, path)

  def http(host: String, port: Int, path: String): Endpoint =
    Endpoint(Scheme.HTTP, host, port, path)

  def http(host: String, path: String): Endpoint =
    Endpoint(Scheme.HTTP, host, 80, path)

  def https(host: String, port: Int, path: String): Endpoint =
    Endpoint(Scheme.HTTPS, host, port, path)

  def https(host: String, path: String): Endpoint =
    Endpoint(Scheme.HTTPS, host, 443, path)
}
