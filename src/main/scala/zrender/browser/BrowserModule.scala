package zrender.browser

import scalaz._
import Scalaz._

case class TabInfo(ctxId: String, tId: String)

final class BrowserModule[F[_]: Monad](B: Browser[F]) {
  def openTab: F[TabInfo] = for {
    ctxId <- B.createCtx
    tId <- B.createTarget(ctxId)
  } yield TabInfo(ctxId, tId)

  def closeTab(tabInfo: TabInfo): F[Unit] = for {
    _ <- B.closeTarget(tabInfo.tId)
    _ <- B.disposeCtx(tabInfo.ctxId)
  } yield ()
}
