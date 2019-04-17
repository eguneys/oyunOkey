package oyun.common

trait Tellable extends Any {

  def !(msg: Any): Unit
  def uniqueId: String

}

object Tellable {

  type Receive = PartialFunction[Any, Unit]

  trait HashCode extends Tellable {
    lazy val uniqueId = Integer.toHexString(hashCode)
  }

  def apply(f: Receive) = new HashCode {
    def !(msg: Any) = f.applyOrElse(msg, doNothing)
  }

  case class Actor(ref: akka.actor.ActorRef) extends AnyVal with Tellable {
    def !(msg: Any) = ref ! msg
    def uniqueId = ref.path.name
  }

  def apply(ref: akka.actor.ActorRef) = Actor(ref)

  private def doNothing(msg: Any) {}

}
