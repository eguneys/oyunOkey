package oyun.round

import scala.concurrent.Promise

import akka.actor._
import akka.pattern.{ ask }
import play.api.libs.json.{ JsObject, Json }
import oyun.common.PimpedJson._

import okey.format.Uci

import actorApi._, round._
import oyun.hub.actorApi.map._
import oyun.hub.DuctMap
import oyun.socket.actorApi.{ Connected => _, _ }
import oyun.socket.Handler
import oyun.socket.Socket
import oyun.user.User
import oyun.game.{ Game, Pov, GameRepo }
import makeTimeout.short

private[round] final class SocketHandler(
  roundMap: DuctMap[Round],
  socketMap: SocketMap,
  hub: oyun.hub.Env,
  messenger: Messenger
) {

  private def controller(
    gameId: String,
    socket: RoundSocket,
    uid: Socket.Uid,
    // ref: PovRef,
    member: Member,
    me: Option[User],
    onPing: () => Unit): Handler.Controller = {

    def send(msg: Any) { roundMap.tell(gameId, msg) }

    def handlePing(o: JsObject) = {
      onPing()
      (o \ "v").asOpt[Int] foreach { v =>
        socket ! VersionCheck(v, member)
      }
    }

    member.playerIdOption.fold[Handler.Controller]({
      case ("p", o) => handlePing(o)
      case ("talk", o) => o str "d" foreach { text =>
        messenger.watcher(gameId, member, text)
      }
    }) { playerId =>
      {
        case ("p", o) => 
          handlePing(o)
        case ("move", o) => parseMove(o) foreach {
          case move =>
            val promise = Promise[Unit]
            promise.future onFailure {
              case _: Exception => socket ! Resync(uid)
            }
            send(HumanPlay(
              playerId, move, promise.some
            ))
            member push ackEvent
        }
        case ("outoftime", _) => send(OutOfTime)
        // case ("bye", _) => socket ! Bye(
        case ("talk", o) => o str "d" foreach { text =>
          messenger.owner(gameId, member, text)
        }
      }
    }
  }

  def watcher(
    gameId: String,
    sideName: String,
    uid: Socket.Uid,
    user: Option[User]): Fu[Option[JsSocketHandler]] =
    GameRepo.pov(gameId, sideName) flatMap {
      _ ?? { join(_, none, uid, user) map some }
    }

  def player(
    pov: Pov,
    uid: Socket.Uid,
    user: Option[User]): Fu[JsSocketHandler] =
    join(pov, Some(pov.playerId), uid, user)


  private def join(
    pov: Pov,
    playerId: Option[String],
    uid: Socket.Uid,
    user: Option[User]): Fu[JsSocketHandler] = {
    val socket = socketMap getOrMake pov.gameId
    socket.ask[Connected](promise => Join(uid = uid,
      user = user,
      side = pov.side,
      playerId = playerId,
      promise = promise)) map {
      case Connected(enum, member) =>
        val onPing: Handler.OnPing =
          if (member.owner) (_, _, _) => {
            Handler.defaultOnPing(socket, member, uid)
            // if (member.owner) socket.playerDo(member.side, _.ping)
          } else Handler.defaultOnPing

        Handler.iteratee(
          hub,
          controller(pov.gameId, socket, uid, member, user, () => onPing(socket, member, uid)),
          member,
          socket,
          uid,
          onPing = onPing
        ) -> enum
    }
  }
    // {
    //   val join = Join(
    //     uid = uid,
    //     user = user,
    //     side = pov.side,
    //     playerId = playerId)
    //   socketHub ? Get(pov.gameId) mapTo manifest[ActorRef] flatMap { socket =>
    //     Handler(socket, uid, join) {
    //       case Connected(enum, member) =>
    //         (controller(pov.gameId, socket, uid, member), enum, member)
    //     }
    //   }
    // }

  private def parseMove(o: JsObject) = for {
    d <- o obj "d"
    key <- d str "key"
    piece = d str "piece"
    group = d str "group"
    pos = d str "pos"
    move <- Uci.Move.fromStrings(key, piece, group, pos)
  } yield move

  private val ackEvent = Json.obj("t" -> "ack")
}
