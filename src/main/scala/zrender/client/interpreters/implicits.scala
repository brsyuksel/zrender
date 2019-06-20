package zrender.client.interpreters

import spinoco.fs2.http.websocket.WebSocketRequest
import spinoco.protocol.http.Uri

import zrender.client.Endpoint

case object implicits {
  private[interpreters] implicit class EndpointOps(e: Endpoint) {
    def httpx: Uri =
      e.scheme match {
        case Endpoint.Scheme.HTTP =>
          Uri.http(e.host, e.port, e.path)
        case Endpoint.Scheme.HTTPS =>
          Uri.https(e.host, e.port, e.path)
      }

    def ws: WebSocketRequest =
      e.scheme match {
        case Endpoint.Scheme.WS =>
          WebSocketRequest.ws(e.host, e.port, e.path)
      }
  }
}
