package oyun.base

import ornicar.scalalib.ValidTypes._

trait OyunException extends Exception {

  val message: String

  override def getMessage = message
  override def toString = message
  
}

object OyunException extends scalaz.syntax.ToShowOps {

  def apply(msg: String) = new OyunException {
    val message = msg
  }

  def apply(msg: Failures): OyunException = apply(msg.shows)
  
}
