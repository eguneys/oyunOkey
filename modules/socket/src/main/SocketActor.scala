package oyun.socket

import scala.concurrent.duration._
import scala.util.Random

import akka.actor._
import play.api.libs.json._

import actorApi._
import oyun.hub.actorApi.{ GetUids, SocketUids }

abstract class SocketActor[M <: SocketMember] extends Socket with Actor {

  val members = scala.collection.mutable.Map.empty[String, M]
  var pong = Socket.initialPong

  val oyunBus = context.system.oyunBus

  // this socket is created during application boot
  // and therefore should delay its publication
  // to ensure the listener is ready (sucks, I know)
  val startsOnApplicationBoot: Boolean = false

  override def preStart {
    if (startsOnApplicationBoot)
      context.system.scheduler.scheduleOnce(1 second) {
        oyunBus.publish(oyun.socket.SocketHub.Open(self), 'socket)
      }
      else oyunBus.publish(oyun.socket.SocketHub.Open(self), 'socket)
  }

  override def postStop() {
    oyunBus.publish(oyun.socket.SocketHub.Close(self), 'socket)
    members.keys foreach eject
  }

  def receiveSpecific: Receive

  def receiveGeneric: Receive = {
    case Ping(uid) => ping(uid)

    case Broom => broom

    case Quit(uid) => quit(uid)

    case GetUids => sender ! SocketUids(members.keySet.toSet)
  }

  def receive = receiveSpecific orElse receiveGeneric

  def notifyAll[A: Writes](t: String, data: A) {
    notifyAll(makeMessage(t, data))
  }

  def notifyAll(t: String) {
    notifyAll(makeMessage(t))
  }

  def notifyAll(msg: JsObject) {
    members.values.foreach(_ push  msg)
  }

  def notifyMember[A: Writes](t: String, data: A)(member: M) {
    member push makeMessage(t, data)
  }

  def ping(uid: String) {
    withMember(uid)(_ push pong)
  }

  def broom {
    members.keys foreach { uid =>
      // broom
    }
  }

  def eject(uid: String) {
    withMember(uid) { member =>
      member.end
      quit(uid)
    }
  }

  def quit(uid: String) {
    members get uid foreach { member =>
      members -= uid
      oyunBus.publish(SocketLeave(uid, member), 'socketDoor)
    }
  }

  private val resyncMessage = makeMessage("resync")

  protected def resync(member: M) {
    import scala.concurrent.duration._
    context.system.scheduler.scheduleOnce((Random nextInt 2000).milliseconds) {
      resyncNow(member)
    }
  }

  protected def resyncNow(member: M) {
    member push resyncMessage
  }

  def addMember(uid: String, member: M) {
    eject(uid)
    members += (uid -> member)
    oyunBus.publish(SocketEnter(uid, member), 'socketDoor)
  }

  def withMember(uid: String)(f: M => Unit) {
    members get uid foreach f
  }
}
