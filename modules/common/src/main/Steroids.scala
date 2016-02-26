package oyun

import ornicar.scalalib

trait Steroids
    extends scalalib.Validation
    with scalalib.Zero.Syntax
    with scalalib.Zero.Instances
    with scalalib.OrnicarOption

    with scalaz.std.OptionFunctions
    with scalaz.syntax.std.ToOptionIdOps

    with BooleanSteroids
    with OptionSteroids

trait BooleanSteroids {
  /*
   * Replaces scalaz boolean ops
   * so ?? works on Zero and not Monoid
   */
  implicit final class OyunPimpedBoolean(self: Boolean) {
    def fold[A](t: => A, f: => A): A = if (self) t else f
  }
}

trait OptionSteroids {

  import scalaz.std.{ option => o }

  implicit final class OyunPimpedOption[A](self: Option[A]) { 
    def err(message: => String): A = self.getOrElse(sys.error(message))
  }
}
