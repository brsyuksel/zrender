package zrender

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.nio.channels.AsynchronousChannelGroup

import scalaz._
import Scalaz._
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._
import fs2.Stream
import spinoco.fs2.http
import spinoco.protocol.http.{HttpRequestHeader, HttpStatusCode}
import pureconfig._
import pureconfig.generic.auto._

import zrender.conf.Config

object main extends App {
  val ES = Executors.newCachedThreadPool(http.util.mkThreadFactory("ACG", true))
  implicit val ACG = AsynchronousChannelGroup.withThreadPool(ES)

  implicit val rt = new DefaultRuntime {}

  def service(r: HttpRequestHeader, b: Stream[Task, Byte]): Stream[Task, http.HttpResponse[Task]] = {
    Stream.emit(http.HttpResponse(HttpStatusCode.Ok).withUtf8Body("hi there!"))
  }

  def server(ip: String, port: Int) =
    http.server(new InetSocketAddress(ip, port))(service).compile.drain

  def readConf: Task[Config] =
    Task(loadConfigOrThrow[Config])

  def prog = for {
    conf <- readConf
    _ <- server(conf.server.ip, conf.server.port)
  } yield ()

  def run(args: List[String]) =
    prog.fold(_ => 1, _ => 0)
}
