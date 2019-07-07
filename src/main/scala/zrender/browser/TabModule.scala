package zrender.browser

import scalaz._
import Scalaz._

final class TabModule[F[_]: Monad](T: Tab[F]) {
  private lazy val scriptTagRgx = "(?is)<script.*?</script>".r

  def getSource(url: String): F[String] = for {
    _ <- T.init
    _ <- T.navigate(url)
    pageSource <- T.source
    clean = scriptTagRgx.replaceAllIn(pageSource, "")
  } yield clean
}
