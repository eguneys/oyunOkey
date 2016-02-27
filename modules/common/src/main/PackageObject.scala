package oyun

import scala.concurrent.Future

import ornicar.scalalib

trait PackageObject extends Steroids with WithFuture {
  lazy val logger = play.api.Logger("oyun")
  def loginfo(s: String) { logger info s }
  def logwarn(s: String) { logger warn s }
  def logerr(s: String) { logger error s }
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

  implicit def OyunFuZero[A: Zero]: Zero[Fu[A]] =
    Zero.instance(fuccess(zero[A]))


  implicit final class OyunPimpedFuture[A](fua: Fu[A]) {

    def void: Funit = fua map (_ => Unit)

    def inject[B](b: => B): Fu[B] = fua map (_ => b)

    def effectFold(fail: Exception => Unit, succ: A => Unit) {
      fua onComplete {
        case scala.util.Failure(e: Exception) => fail(e)
        case scala.util.Failure(e) => throw e
        case scala.util.Success(e) => succ(e)
      }
    }
  }

  object makeTimeout {
    import akka.util.Timeout
    import scala.concurrent.duration._

    implicit val short = seconds(1)
    implicit val large = seconds(5)

    def apply(duration: FiniteDuration) = Timeout(duration)
    def seconds(s: Int): Timeout = Timeout(s.seconds)
  }
}
