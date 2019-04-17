package oyun.round

import scala.concurrent.duration._

import akka.actor._
import akka.pattern.{ ask, pipe }
import play.api.libs.iteratee._
import play.api.libs.json._

import actorApi._
import oyun.common.LightUser
import oyun.game.Event
import oyun.hub.TimeBomb
import oyun.hub.Trouper
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket._
import okey.{ Sides, Side }


private[round] final class RoundSocket(
  gameId: String,
  dependencies: RoundSocket.Dependencies,
  history: History) extends SocketTrouper[Member](dependencies.system, dependencies.uidTtl) {

  import dependencies._

  // private val timeBomb = new TimeBomb(socketTimeout)

  private var delayedCrowdNotification = false

  private final class Player(side: Side) {

    // when the player has been seen online for the last time
    private var time: Long = nowMillis
    // whether the player closed the window intentionally
    private var bye: Int = 0

    var userId = none[String]

    var isAi = false

    def ping {
      isGone foreach { _ ?? notifyGone(side, false) }
      if (bye > 0) bye = bye - 1
      time = nowMillis
    }

    def setBye {
      bye = 3
    }
    private def isBye = bye > 0

    def isGone = if (time < (nowMillis - isBye.fold(ragequitTimeout, disconnectTimeout).toMillis))
      fuccess(!isAi)
    else fuccess(false)
  }

  private val players = Sides(side => new Player(side))

  oyun.game.GameRepo game gameId map SetGame.apply foreach this.!


  def receiveSpecific: Trouper.Receive = {

    case SetGame(Some(game)) =>
      players sideMap { case (side, p) =>
        p.isAi = game.player(side).isAi
      }

    // case PingVersion(uid, v) =>
    //   timeBomb.delay
    //   ping(uid)
    //   ownerOf(uid) foreach { o =>
    //     playerDo(o.side, _.ping)
    //   }
    //   withMember(uid) { member =>
    //     (history getEventsSince v).fold(resyncNow(member))(batch(member, _))
    //   }

    // case Broom =>
    //   broom
    //   if (timeBomb.boom) self ! PoisonPill
    //   else playersGet(_.isGone) sideMap { case (side, isGone) => isGone foreach { _ ?? notifyGone(side, true) } }
      
    case GetSocketStatus(promise) =>
      playersGet(_.isGone).sequenceSides foreach { sidesIsGone =>

        val sidesOnGame = players sideMap { case (side, p) => ownerOf(side).isDefined }
        promise success SocketStatus(
          version = history.getVersion,
          sidesIsGone = sidesIsGone,
          sidesOnGame = sidesOnGame)
      }

    case Join(uid, user, side, playerId, promise) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = Member(channel, user, side, playerId)
      addMember(uid, member)
      notifyCrowd
      playerDo(side, _.ping)
      promise success Connected(enumerator, member)

    case eventList: EventList => notify(eventList.events)

    case oyun.chat.actorApi.ChatLine(chatId, line) => notify(List(line match {
      case l: oyun.chat.UserLine => Event.UserMessage(l, chatId endsWith "/w")
      case l: oyun.chat.PlayerLine => Event.PlayerMessage(l)
    }))

    // case Quit(uid) =>
    //   members get uid foreach { member =>
    //     quit(uid)
    //     notifyCrowd
    //   }

    case NotifyCrowd =>
      delayedCrowdNotification = false

      val sidesOnGame = players sideMap { case (side, p) => ownerOf(side).isDefined }
      val event = Event.Crowd(
        sidesOnGame = sidesOnGame
      )
      notifyAll(event.typ, event.data)
  }

  def notifyCrowd {
    if (!delayedCrowdNotification) {
      delayedCrowdNotification = true
      system.scheduler.scheduleOnce(1 second)(this ! NotifyCrowd)
    }
  }

  def notify(events: Events) {
    val vevents = history addEvents events
    members.values foreach { m => batch(m, vevents) }
  }

  def batch(member: Member, vevents: List[VersionedEvent]) {
    vevents match {
      case Nil =>
      case List(one) => member push one.jsFor(member)
      case many => member push makeMessage("b", many map (_ jsFor member))
    }
  }

  def notifyOwner[A: Writes](side: Side, t: String, data: A) {
    ownerOf(side) foreach { m =>
      m push makeMessage(t, data)
    }
  }

  def notifyGone(side: Side, gone: Boolean) {
    //  notify all gone
    Side.all.filterNot(side==) foreach { notifyOwner(_, "gone", Json.obj(side.name -> gone)) }
  }

  def ownerOf(side: Side): Option[Member] =
    members.values find { m => m.owner && m.side == side }

  def ownerOf(uid: String): Option[Member] =
    members get uid filter (_.owner)

  private def playersGet[A](getter: Player => A): Sides[A] = players map getter

  private def playerDo(side: Side, effect: Player => Unit) {
    effect(players(side))
  }
}

object RoundSocket {

  private[round] case class Dependencies(
    system: ActorSystem,
    lightUser: LightUser.Getter,
    uidTtl: FiniteDuration,
    disconnectTimeout: FiniteDuration,
    ragequitTimeout: FiniteDuration)
}


