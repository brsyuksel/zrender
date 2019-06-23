package zrender.conf

case class Chrome(bin: String, port: Int)

case class ZRender(blockedUrls: Option[List[String]], reqHeader: Boolean, resHeader: Boolean)

case class Config(chrome: Chrome, zrender: ZRender)
