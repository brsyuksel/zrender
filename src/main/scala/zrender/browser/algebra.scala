package zrender.browser

trait Browser[F[_]] {
  def createCtx: F[String]
  def disposeCtx(ctxId: String): F[Unit]

  def createTarget(ctxId: String): F[String]
  def closeTarget(tId: String): F[Unit]
}

trait Tab[F[_]] {
  def navigate(url: String): F[String]
}
