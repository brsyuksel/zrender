package zrender.browser.interpreters

import io.circe.Decoder

import zrender.client.JsDecoder

case class ChromeResponse(
  browser: String,
  protocolVersion: String,
  userAgent: String,
  v8Version: String,
  webKitVersion: String,
  webSocketDebuggerUrl: String
)
object ChromeResponse {
  implicit val dec: Decoder[ChromeResponse] =
    Decoder.forProduct6(
      "Browser",
      "Protocol-Version",
      "User-Agent",
      "V8-Version",
      "WebKit-Version",
      "webSocketDebuggerUrl"
    )(ChromeResponse.apply)

  implicit val jsDec: JsDecoder[ChromeResponse] = new JsDecoder[ChromeResponse] {
    def decoder[B] = dec.asInstanceOf[B]
  }
}
