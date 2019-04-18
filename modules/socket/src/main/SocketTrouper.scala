package oyun.socket

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Random

import akka.actor.{ Deploy => _, _ }
import play.api.libs.json._

import actorApi._
import oyun.hub.Trouper
import oyun.hub.actorApi.{ Deploy, GetUids, SocketUids }
import oyun.memo.ExpireSetMemo

abstract class SocketTrouper[M <: SocketMember](
  protected val system: akka.actor.ActorSystem,
  protected val uidTtl: Duration)
    extends Socket with Trouper {

  protected val members = scala.collection.mutable.Map.empty[String, M]
  protected val aliveUids = new ExpireSetMemo(uidTtl)

  var pong = Socket.initialPong

  val oyunBus = system.oyunBus

  // this socket is created during application boot
  // and therefore should delay its publication
  // to ensure the listener is ready (sucks, I know)
  // val startsOnApplicationBoot: Boolean = false

  // override def preStart {
  //   if (startsOnApplicationBoot)
  //     system.scheduler.scheduleOnce(1 second) {
  //       oyunBus.publish(oyun.socket.SocketHub.Open(this), 'socket)
  //     }
  //     else oyunBus.publish(oyun.socket.SocketHub.Open(this), 'socket)
  // }

  override def stop() {
    super.stop()
    // oyunBus.publish(oyun.socket.SocketHub.Close(this), 'socket)
    members.keys foreach ejectUidString
  }

  // protected val receiveTrouper: PartialFunction[Any, Unit] = {

  // }

  def receiveSpecific: PartialFunction[Any, Unit]

  def receiveGeneric: PartialFunction[Any, Unit] = {
    // case Ping(uid) => ping(uid)

    case Broom =>
      broom

    case Quit(uid) => quit(uid)

    // case GetUids => sender ! SocketUids(members.keySet.toSet)

    case Resync(uid) => resync(uid)

    case d: Deploy => onDeploy(d)
  }

  val process = receiveSpecific orElse receiveGeneric

  def setAlive(uid: Socket.Uid): Unit = aliveUids put uid.value

  def notifyAll[A: Writes](t: String, data: A) {
    notifyAll(makeMessage(t, data))
  }

  def notifyAll(t: String) {
    notifyAll(makeMessage(t))
  }

  def notifyAll(msg: JsObject) {
    members.values.foreach(_ push  msg)
  }

  def notifyAllAsync[A: Writes](t: String, data: A) = Future {
    notifyAll(t, data)
  }

  def notifyAllAsync(msg: JsObject) = Future {
    notifyAll(msg)
  }

  def notifyMember[A: Writes](t: String, data: A)(member: M) {
    member push makeMessage(t, data)
  }

  // def ping(uid: String) {
  //   withMember(uid)(_ push pong)
  // }

  protected def broom: Unit =
    members.keys foreach { uid =>
      // broom
      if (!aliveUids.get(uid)) {
        ejectUidString(uid)
      }
    }

  protected def ejectUidString(uid: String): Unit = eject(Socket.Uid(uid))

  def eject(uid: Socket.Uid) {
    withMember(uid) { member =>
      member.end
      quit(uid)
    }
  }

  def quit(uid: Socket.Uid) {
    members get uid.value foreach { member =>
      members -= uid.value
      oyunBus.publish(SocketLeave(uid, member), 'socketLeave)
      afterQuit(uid, member)
    }
  }

  protected def afterQuit(uid: Socket.Uid, member: M): Unit = {}

  def onDeploy(d: Deploy) {
    notifyAll(makeMessage(d.key))
  }

  protected val resyncMessage = makeMessage("resync")

  protected def resync(member: M) {
    import scala.concurrent.duration._
    system.scheduler.scheduleOnce((Random nextInt 2000).milliseconds) {
      resyncNow(member)
    }
  }

  protected def resync(uid: Socket.Uid) {
    withMember(uid)(resync)
  }

  protected def resyncNow(member: M) {
    member push resyncMessage
  }

  def addMember(uid: Socket.Uid, member: M) {
    eject(uid)
    members += (uid.value -> member)
    oyunBus.publish(SocketEnter(uid, member), 'socketEnter)
  }

  def withMember(uid: Socket.Uid)(f: M => Unit) {
    members get uid.value foreach f
  }
}
