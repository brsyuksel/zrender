package zrender.browser

import scalaz._
import Scalaz._

import org.scalatest._
import org.scalatest.prop.PropertyChecks
import org.scalacheck._
import org.scalacheck.Prop.forAll

class BrowserModuleSpec extends FlatSpec with PropertyChecks {
  def mockBrowser[F[_]: Applicative](ctxId: String): Browser[F] = new Browser[F] {
    def createCtx: F[String] = ctxId.pure[F]
    def disposeCtx(ctxId: String): F[Unit] = ().pure[F]
    def createTarget(ctxId: String): F[String] = ("tId-" |+| ctxId).pure[F]
    def closeTarget(tId: String): F[Unit] = ().pure[F]
  }

  def mockBrowserError[F[_]](ctxId: String)(implicit F: MonadError[F, String]): Browser[F] = new Browser[F] {
    def createCtx: F[String] = F.pure(ctxId)
    def createTarget(ctxId: String): F[String] = F.pure(ctxId)

    def disposeCtx(cId: String): F[Unit] =
      if(cId == ctxId) F.pure(())
      else F.raiseError("invalid ctxId")

    def closeTarget(tId: String): F[Unit] =
      if(tId == ("tId-" |+| ctxId)) F.pure(())
      else F.raiseError("invalid tId")
  }

  "BrowserModule.openTab" should "return TabInfo data provided by mockBrowser[Id]" in {
    forAll { ctxId: String =>
      val mock = mockBrowser[Id](ctxId)
      val module = new BrowserModule(mock)

      assert(module.openTab === TabInfo(ctxId, "tId-" |+| ctxId).pure[Id])
    }
  }

  type E[A] = String \/ A

  "BrowserModule.closeTab" should "return ().right provided by mockBrowserError[Either]" in {
    forAll { ctxId: String =>
      val mock = mockBrowserError[E](ctxId)
      val module = new BrowserModule(mock)

      assert(module.closeTab(TabInfo(ctxId, "tId-" |+| ctxId)) === \/-(()))
    }
  }

  "BrowserModule.closeTab" should "return left for invalid ctxId" in {
    forAll { ctxId: String =>
      val mock = mockBrowserError[E](ctxId)
      val module = new BrowserModule(mock)

      assert(module.closeTab(TabInfo("-" |+| ctxId, "tId-" |+| ctxId)) === -\/("invalid ctxId"))
    }
  }

  "BrowserModule.closeTab" should "return left for invalid tId" in {
    forAll { ctxId: String =>
      val mock = mockBrowserError[E](ctxId)
      val module = new BrowserModule(mock)

      assert(module.closeTab(TabInfo(ctxId, ctxId)) === -\/("invalid tId"))
    }
  }
}
