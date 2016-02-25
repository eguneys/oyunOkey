package oyun.common

trait OyunException extends Exception {
  val message: String

  override def getMessage = message
  override def toString = message
}

object OyunException {
  def apply(msg: String): OyunException = new OyunException {
    val message = msg
  }
}
