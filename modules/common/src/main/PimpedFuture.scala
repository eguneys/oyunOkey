package oyun

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._

object PimpedFuture {
  private type Fu[A] = Future[A]
  private type Funit = Fu[Unit]

  final class OyunPimpedFuture[A](val fua: Fu[A]) extends AnyVal {

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

    def andThenAnyway(sideEffect: => Unit): Fu[A] = {
      fua onComplete {
        case scala.util.Failure(_) => sideEffect
        case scala.util.Success(_) => sideEffect
      }
      fua
    }

    def addFailureEffect(effect: Exception => Unit) = {
      fua onFailure {
        case e: Exception => effect(e)
      }
      fua
    }

    def addEffect(effect: A => Unit) = {
      fua foreach effect
      fua
    }

    def awaitSeconds(seconds: Int): A = {
      import scala.concurrent.duration._
      scala.concurrent.Await.result(fua, seconds.seconds)
    }

    def withTimeout(duration: FiniteDuration, error: => Throwable)(implicit system: akka.actor.ActorSystem): Fu[A] = {
      Future firstCompletedOf Seq(fua,
        akka.pattern.after(duration, system.scheduler)(Future failed error))
    }
  }
}
