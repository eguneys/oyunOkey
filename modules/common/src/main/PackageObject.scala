package oyun

import scala.concurrent.Future

import ornicar.scalalib

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
  import scalalib.Zero

  implicit def execontext = play.api.libs.concurrent.Execution.defaultContext

  implicit def LilaFuZero[A: Zero]: Zero[Fu[A]] =
    Zero.instance(fuccess(zero[A]))

  object makeTimeout {
    import akka.util.Timeout
    import scala.concurrent.duration._

    implicit val short = seconds(1)
    implicit val large = seconds(5)

    def apply(duration: FiniteDuration) = Timeout(duration)
    def seconds(s: Int): Timeout = Timeout(s.seconds)
  }
}
