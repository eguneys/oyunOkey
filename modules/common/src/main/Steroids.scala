package oyun

import ornicar.scalalib
import ornicar.scalalib.Zero

import oyun.base._

import play.api.libs.json.{ JsValue, JsObject }

trait Steroids
    extends OyunTypes
    with scalalib.Validation
    with scalalib.Common
    // with scalalib.OrnicarMonoid.Instances
    with scalalib.Zero.Syntax
    with scalalib.Zeros
    with scalalib.OrnicarOption

    with scalaz.std.StringInstances

    with scalaz.std.OptionInstances
    with scalaz.std.OptionFunctions
    with scalaz.syntax.std.ToOptionIdOps

    with scalaz.std.ListInstances

    with scalaz.syntax.ToIdOps
    with scalaz.syntax.ToValidationOps
    with scalaz.syntax.ToTraverseOps
    with scalaz.syntax.ToShowOps

    with BooleanSteroids
    with OptionSteroids

    with JodaTimeSteroids {

      // @inline implicit def toPimpedActorSystem(a: akka.actor.ActorSystem) = new PimpedActorSystem(a)

      @inline implicit def toPimpedJsObject(jo: JsObject) = new PimpedJsObject(jo)
      @inline implicit def toPimpedString(s: String) = new PimpedString(s)

    }


final class PimpedActorSystem(private val a: akka.actor.ActorSystem) extends AnyVal {
  def oyunBus = oyun.common.Bus(a)
}

trait JodaTimeSteroids {
  import org.joda.time.DateTime
  implicit final class OyunPimpedDateTime(date: DateTime) {
    def getSeconds: Long = date.getMillis / 1000
  }
}

trait BooleanSteroids {
  /*
   * Replaces scalaz boolean ops
   * so ?? works on Zero and not Monoid
   */
  implicit final class OyunPimpedBoolean(self: Boolean) {

    def ??[A](a: => A)(implicit z: Zero[A]): A = if (self) a else Zero[A].zero

    def !(f: => Unit) = if (self) f

    def fold[A](t: => A, f: => A): A = if (self) t else f

    def option[A](a: => A): Option[A] = if (self) Some(a) else None
  }
}

trait OptionSteroids {

  import scalaz.std.{ option => o }

  implicit final class OyunPimpedOption[A](self: Option[A]) { 

    def |(a: => A): A = self getOrElse a

    def unary_~(implicit z: Zero[A]): A = self getOrElse z.zero

    def err(message: => String): A = self.getOrElse(sys.error(message))

    def has(a: A) = self contains a
  }
}
