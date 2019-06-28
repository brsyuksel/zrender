package zrender.proc

trait ProcStatus[F[_]] {
  def isAlive: F[Boolean]
  def exitValue: F[Option[Int]]
  def destroy: F[Unit]
}

trait BGProc[F[_]] {
  def run(cmd: String): F[ProcStatus[F]]
}
