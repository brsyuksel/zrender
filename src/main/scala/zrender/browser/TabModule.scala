package zrender.browser

import scalaz._
import Scalaz._

final class TabModule[F[_]: Monad](T: Tab[F]) {
  private lazy val scriptTagRgx = "(?i)<script.*?</script>".r

  def getSource(url: String): F[String] = for {
    pageSource <- T.navigate(url)
    clean <- scriptTagRgx.replaceAllIn(pageSource, "").pure[F]
  } yield clean
}
