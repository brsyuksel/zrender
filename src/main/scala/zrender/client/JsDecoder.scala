package zrender.client

trait JsDecoder[A] {
  def decoder[B]: B
}
object JsDecoder {
  def apply[A: JsDecoder]: JsDecoder[A] = implicitly[JsDecoder[A]]
}
