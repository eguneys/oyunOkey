package oyun

import scala.concurrent.Future

import ornicar.scalalib
import scalaz.{ Monad, Monoid, OptionT, ~> }

trait PackageObject extends Steroids with WithFuture {
  lazy val logger = play.api.Logger("oyun")
  def loginfo(s: String) { logger info s }
  def logwarn(s: String) { logger warn s }
  def logerr(s: String) { logger error s }

  implicit final def runOptionT[F[+_], A](ot: OptionT[F, A]): F[Option[A]] = ot.run

  // from scalaz. We don't want to import all OptionTFunctions, because of the clash with `some`
  def optionT[M[_]] = new (({ type λ[α] = M[Option[α]] })#λ ~>({ type λ[α] = OptionT[M, α] })#λ) {
    def apply[A](a: M[Option[A]]) = new OptionT[M, A](a)
  }

  def parseIntOption(str: String): Option[Int] = try {
    Some(java.lang.Integer.parseInt(str))
  } catch {
    case e: NumberFormatException => None
  }
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


  implicit def OyunFutureMonad = new Monad[Fu] {
    override def map[A, B](fa: Fu[A])(f: A => B) = fa map f
    def point[A](a: => A) = fuccess(a)
    def bind[A, B](fa: Fu[A])(f: A => Fu[B]) = fa flatMap f
  }

  implicit def OyunFuZero[A: Zero]: Zero[Fu[A]] =
    Zero.instance(fuccess(zero[A]))

  implicit final class OyunTraversableFuture[A, M[X] <: TraversableOnce[X]](t: M[Fu[A]]) {
    def sequenceFu(implicit cbf: scala.collection.generic.CanBuildFrom[M[Fu[A]], A, M[A]]) =
      Future sequence t
  }

  implicit final class OyunPimpedFuture[A](fua: Fu[A]) {

    def >>-(sideEffect: => Unit): Fu[A] = fua andThen {
      case _ => sideEffect
    }

    def >>[B](fub: => Fu[B]): Fu[B] = fua flatMap (_ => fub)

    def void: Funit = fua map (_ => Unit)

    def inject[B](b: => B): Fu[B] = fua map (_ => b)

    def effectFold(fail: Exception => Unit, succ: A => Unit) {
      fua onComplete {
        case scala.util.Failure(e: Exception) => fail(e)
        case scala.util.Failure(e) => throw e
        case scala.util.Success(e) => succ(e)
      }
    }

    def addEffect(effect: A => Unit) = fua ~ (_ foreach effect)

    def awaitSeconds(seconds: Int): A = {
      import scala.concurrent.duration._
      scala.concurrent.Await.result(fua, seconds.seconds)
    }
  }

  implicit final class OyunPimpedActorSystem(self: akka.actor.ActorSystem) {
    def oyunBus = oyun.common.Bus(self)
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
