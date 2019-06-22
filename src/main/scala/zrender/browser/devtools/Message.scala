package zrender.browser.devtools

import io.circe.Encoder
import io.circe.generic.semiauto._

sealed trait Domain
sealed abstract class Method(domain: Domain, name: String) {
  override def toString: String = s"$domain.$name"
}

case object Target extends Domain {
  case object CreateBrowserContext extends Method(this, "createBrowserContext")
  case object CreateTarget extends Method(this, "createTarget")
  case object DisposeBrowserContext extends Method(this, "disposeBrowserContext")
  case object CloseTarget extends Method(this, "closeTarget")
}

case object Security extends Domain {
  case object Enable extends Method(this, "enable")
  case object SetIgnoreCertificateErrors extends Method(this, "setIgnoreCertificateErrors")
}

case object Runtime extends Domain {
  case object Enable extends Method(this, "enable")
  case object Evaluate extends Method(this, "evaluate")
}

case object Page extends Domain {
  case object Enable extends Method(this, "enable")
  case object Navigate extends Method(this, "navigate")
}

case object Network extends Domain {
  case object Enable extends Method(this, "enable")
  case object SetBlockedURLs extends Method(this, "setBlockedURLs")
  case object SetBypassServiceWorker extends Method(this, "setBypassServiceWorker")
  case object SetExtraHTTPHeaders extends Method(this, "setExtraHTTPHeaders")
  case object SetUserAgentOverride extends Method(this, "setUserAgentOverride")
}

case object DOM extends Domain {
  case object Enable extends Method(this, "enable")
}

case class Message[A](id: Int, method: String, params: A)
object Message {
  implicit def enc[A: Encoder]: Encoder[Message[A]] = deriveEncoder

  implicit def method2Str(m: Method): String = m.toString

  private type M[A] = Int => Message[A]

  def createCtx: M[Map[String, String]] =
    Message(_, Target.CreateBrowserContext, Map.empty)
  def createTarget(ctxId: String): M[Map[String, String]] =
    Message(_, Target.CreateTarget, Map("browserContextId" -> ctxId, "url" -> "about:blank"))
  def disposeCtx(ctxId: String): M[Map[String, String]] =
    Message(_, Target.DisposeBrowserContext, Map("browserContextId" -> ctxId))
  def closeTarget(tId: String): M[Map[String, String]] =
    Message(_, Target.CloseTarget, Map("targetId" -> tId))

  def enableSecurity: M[Map[String, String]] =
    Message(_, Security.Enable, Map.empty)
  def ignoreCertErrs: M[Map[String, Boolean]] =
    Message(_, Security.SetIgnoreCertificateErrors, Map("ignore" -> true))

  def enableRuntime: M[Map[String, String]] =
    Message(_, Runtime.Enable, Map.empty)
  def evaluate(exp: String): M[Map[String, String]] =
    Message(_, Runtime.Evaluate, Map("expression" -> exp))

  def enablePage: M[Map[String, String]] =
    Message(_, Page.Enable, Map.empty)
  def navigate(url: String): M[Map[String, String]] =
    Message(_, Page.Navigate, Map("url" -> url))

  def enableNetwork: M[Map[String, String]] =
    Message(_, Network.Enable, Map.empty)
  def blockUrls(urls: List[String]): M[Map[String, List[String]]] =
    Message(_, Network.SetBlockedURLs, Map("urls" -> urls))
  def bypassWorkers: M[Map[String, Boolean]] =
    Message(_, Network.SetBypassServiceWorker, Map("bypass" -> true))
  def extraHeaders(headers: Map[String, String]): M[Map[String, Map[String, String]]] =
    Message(_, Network.SetExtraHTTPHeaders, Map("headers" -> headers))
  def userAgent(uA: String): M[Map[String, String]] =
    Message(_, Network.SetUserAgentOverride, Map("userAgent" -> uA))

  def enableDOM: M[Map[String, String]] =
    Message(_, DOM.Enable, Map.empty)
}
