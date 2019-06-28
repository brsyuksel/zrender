package zrender.proc

import sys.process.Process

import scalaz._
import Scalaz._

final class SysProcess[F[_]: Monad] extends BGProc[F] {
  def run(cmd: String): F[ProcStatus[F]] = {
    val p = Process(cmd).run
    val st = new ProcStatus[F] {
      def isAlive: F[Boolean] =
        p.isAlive.pure[F]

      def exitValue: F[Option[Int]] =
        isAlive.ifM(none.pure[F], p.exitValue().some.pure[F])

      def destroy: F[Unit] =
        p.destroy().pure[F]
    }

    st.pure[F]
  }
}
