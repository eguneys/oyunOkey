package oyun.socket

import scala.concurrent.Promise

import play.api.libs.json._

object Socket extends Socket {

  case class Uid(value: String) extends AnyVal

  case class GetVersion(promise: Promise[Int])

  val initialPong = makeMessage("n")
  val emptyPong = JsNumber(0)
}

private[socket] trait Socket {
  def makeMessage[A](t: String, data: A)(implicit writes: Writes[A]): JsObject =
    JsObject(List("t" -> JsString(t), "d" -> writes.writes(data)))

  def makeMessage(t: String): JsObject = JsObject(List("t" -> JsString(t)))

  def makePong(n: Int) = makeMessage("n", n)
}
