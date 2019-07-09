package zrender.browser

import scalaz._
import Scalaz._

import org.scalatest._
import org.scalatest.prop.PropertyChecks
import org.scalacheck._
import org.scalacheck.Prop.forAll

class TabModuleSpec extends FlatSpec with PropertyChecks {
  def mockTab[F[_]: Applicative](body: String): Tab[F] = new Tab[F] {
    def init: F[Unit] = ().pure[F]
    def navigate(url: String): F[Unit] = ().pure[F]
    def source: F[String] = body.pure[F]
  }

  ".getSource" should "return plain text" in {
    forAll { s: String =>
      val mock = mockTab[Id](s)
      val module = new TabModule(mock)

      assert(module.getSource("url") == s)
    }
  }

  ".getSource" should "return body with no scripts" in {
    val b = """body<script>1</script>text<script type="text/javascript">2</script>"""
    val mock = mockTab[Id](b)
    val module = new TabModule(mock)

    assert(module.getSource("url") == "bodytext")
  }
}
