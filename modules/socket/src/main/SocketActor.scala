package oyun.socket

import akka.actor._
import play.api.libs.json._

import actorApi._
import oyun.hub.actorApi.{ GetUids, SocketUids }

abstract class SocketActor[M <: SocketMember] extends Socket with Actor {

  val members = scala.collection.mutable.Map.empty[String, M]
  val pong = Socket.initialPong


  def receiveSpecific: Receive

  def receiveGeneric: Receive = {
    case Ping(uid) => ping(uid)
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

  def eject(uid: String) {
    withMember(uid) { member =>
      member.end
      quit(uid)
    }
  }

  def quit(uid: String) {
    members get uid foreach { member =>
      members -= uid
    }
  }

  def addMember(uid: String, member: M) {
    eject(uid)
    members += (uid -> member)
  }

  def withMember(uid: String)(f: M => Unit) {
    members get uid foreach f
  }
}
