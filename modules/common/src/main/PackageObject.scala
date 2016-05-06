package oyun

import scala.concurrent.Future

import ornicar.scalalib
import scalaz.{ Monad, Monoid, OptionT, ~> }

trait PackageObject extends Steroids with WithFuture {

  def !![A](msg: String): Valid[A] = msg.failureNel[A]

  def nowNanos: Long = System.nanoTime()
  def nowMillis: Long = System.currentTimeMillis()
  def nowSeconds: Int = (nowMillis / 1000).toInt

  implicit final def runOptionT[F[+_], A](ot: OptionT[F, A]): F[Option[A]] = ot.run

  // from scalaz. We don't want to import all OptionTFunctions, because of the clash with `some`
  def optionT[M[_]] = new (({ type λ[α] = M[Option[α]] })#λ ~>({ type λ[α] = OptionT[M, α] })#λ) {
    def apply[A](a: M[Option[A]]) = new OptionT[M, A](a)
  }

  implicit final class OyunPimpedString(s: String) {
    def boot[A](v: => A): A = { oyun.log.boot.info(s); v }
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

  implicit def SprayPimpedFuture[T](fut: Future[T]) =
    new spray.util.pimps.PimpedFuture[T](fut)
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

  implicit def OyunPimpedFuture[A](fua: Fu[A]): PimpedFuture.OyunPimpedFuture[A] =
    new PimpedFuture.OyunPimpedFuture(fua)

  implicit final class OyunPimpedFutureOption[A](fua: Fu[Option[A]]) {
    def flatten(msg: => String): Fu[A] = fua flatMap {
      _.fold[Fu[A]](fufail(msg))(fuccess(_))
    }
  }

  implicit final class OyunPimpedFutureBoolean[A](fua: Fu[Boolean]) {
    def unary_! = fua map (!_)
  }

  implicit final class OyunPimpedBooleanWithFuture(self: Boolean) {
    def optionFu[A](v: => Fu[A]): Fu[Option[A]] = if (self) v map (_.some) else fuccess(none)
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
