package zrender.conf

import pureconfig._

case class Chrome(bin: String, port: Int)
object Chrome {
  implicit val chrome = ConfigReader.forProduct2("bin", "port")(Chrome.apply)
}

case class ZRender(blockedUrls: Option[List[String]], reqHeader: Boolean, resHeader: Boolean, reqInterval: Double)
object ZRender {
  implicit val zrender = ConfigReader.forProduct4("blocked-urls", "req-header", "res-header", "req-interval")(ZRender.apply)
}

case class Server(ip: String, port: Int)
object Server {
  implicit val server = ConfigReader.forProduct2("ip", "port")(Server.apply)
}

case class Config(chrome: Chrome, server: Server, zrender: ZRender)
object Config {
  implicit val config = ConfigReader.forProduct3("chrome", "server", "zrender")(Config.apply)
}
