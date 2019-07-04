package zrender

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.nio.channels.AsynchronousChannelGroup
import java.net.URI

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import scalaz.zio.interop.scalaz72._
import fs2.Stream
import spinoco.fs2.http
import spinoco.fs2.http.HttpResponse
import spinoco.protocol.http.{HttpRequestHeader, HttpStatusCode, Uri, Scheme, HostPort}
import spinoco.protocol.http.header._
import spinoco.protocol.http.header.value.AgentVersion
import spinoco.protocol.mime.{ContentType, MediaType, MIMECharset}
import spinoco.fs2.http.routing._
import pureconfig._

import zrender.conf.Config
import zrender.proc.SysProcess
import zrender.client.Endpoint
import zrender.client.interpreters._
import zrender.browser._
import zrender.browser.interpreters._

object main extends App {
  val ES = Executors.newCachedThreadPool(http.util.mkThreadFactory("ACG", true))
  implicit val ACG = AsynchronousChannelGroup.withThreadPool(ES)
  implicit val rt = new DefaultRuntime {}

  def getUri(r: HttpRequestHeader): String \/ String =
    if(r.path.segments.length < 2) "not an uri".left[String]
    else {
      val scheme :: host :: path = r.path.segments.toList
      val uri = Uri(Scheme(scheme.replace(":", "")), HostPort(host, None), Uri.Path(true, false, path), r.query)
      \/ fromEither uri.stringify.toEither.leftMap(_.message)
    }

  def getUserAgent(r: HttpRequestHeader): String =
    r.headers.collectFirst { case `User-Agent`(AgentVersion(a)) => a }.getOrElse("zrender/1.0.0")

  def getSource(c: Config, r: ChromeResponse)(uri: String, userAgent: String): Task[String] =
    for {
      httpClient <- http.client[Task]()
      wsClient = new StreamWebSocketClient(httpClient)
      u = URI.create(r.webSocketDebuggerUrl)
      browser = new ChromeBrowser(Endpoint.ws("localhost", c.chrome.port, u.getPath))(wsClient)
      bm = new BrowserModule(browser)
      tabInfo <- bm.openTab
      tab = new ChromeTab(Endpoint.ws("localhost", 9222, "/devtools/page/" |+| tabInfo.tId), userAgent, c.zrender)(wsClient)
      tm = new TabModule(tab)
      source <- tm.getSource(uri)
      _ <- bm.closeTab(tabInfo)
    } yield source

  type Handler = (HttpRequestHeader, Stream[Task, Byte]) => Stream[Task, HttpResponse[Task]]

  def handler(c: Config, r: ChromeResponse)(fn: (String, String) => Task[String]): Handler =
    (req, body) => getUri(req) match {
      case -\/(err) =>
        Stream.emit(HttpResponse(HttpStatusCode.ServiceUnavailable))
      case \/-(uri) =>
        val header = `Content-Type`(ContentType.TextContent(MediaType.`text/html`, Some(MIMECharset.`UTF-8`)))
        Stream
          .eval(fn(uri, getUserAgent(req)))
          .map(r => HttpResponse(HttpStatusCode.Ok).appendHeader(header).withUtf8Body(r))
    }

  def server(c: Config, r: ChromeResponse)(fn: Handler): Task[Unit] =
    http.server(new InetSocketAddress(c.server.ip, c.server.port))(fn).compile.drain

  def r[A](fn: (Config, ChromeResponse) => A) = Reader[(Config, ChromeResponse), A]{
    case (c: Config, res: ChromeResponse) => fn(c, res)
  }

  def getServer: Reader[(Config, ChromeResponse), Task[Unit]] =
    for {
      sFn <- r(getSource)
      hFn <- r(handler)
      servFn <- r(server)
    } yield servFn(hFn(sFn))

  def readConf: Task[Config] =
    Task(loadConfigOrThrow[Config])

  def prog = for {
    conf <- readConf
    cmd = conf.chrome.bin |+| " --no-sandbox --headless --disable-gpu --remote-debugging-port=" |+| conf.chrome.port.toString
    proc = new SysProcess[Task]
    chrome <- proc.run(cmd)
    httpClient <- http.client[Task]()
    restClient = new StreamRestClient(httpClient)
    chromeRes <- restClient.get[ChromeResponse](Endpoint.http("localhost", conf.chrome.port, "/json/version"))
    _ <- getServer.run(conf, chromeRes)
    _ <- chrome.destroy
    exitCode <- chrome.exitValue
  } yield ()

  def run(args: List[String]) =
    prog.catchAll(t => Task(println(t))).fold(_ => 1, _ => 0)
}
