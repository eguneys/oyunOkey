package oyun

import ornicar.scalalib
import ornicar.scalalib.Zero

trait Steroids
    extends scalalib.Validation
    with scalalib.Common
    with scalalib.OrnicarMonoid.Instances
    with scalalib.Zero.Syntax
    with scalalib.Zero.Instances
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
  }
}
