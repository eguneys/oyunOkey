package oyun

import scala.concurrent.Future

trait PackageObject extends Steroids with WithFuture {
}

trait WithFuture {
  type Fu[+A] = Future[A]
  type Funit = Fu[Unit]

  def fuccess[A](a: A) = Future successful a
  def fufail[A <: Throwable, B](a: A): Fu[B] = Future failed a
  def fufail[A](a: String): Fu[A] = fufail(common.OyunException(a))
  val funit = fuccess(())
}

trait WithPlay { self: PackageObject =>
  implicit def execontext = play.api.libs.concurrent.Execution.defaultContext
}
