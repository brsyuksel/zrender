package zrender.conf

case class Chrome(bin: String, port: Int)

case class ZRender(blockedUrls: Option[List[String]], reqHeader: Boolean, resHeader: Boolean)

case class Server(ip: String, port: Int)

case class Config(chrome: Chrome, server: Server, zrender: ZRender)
