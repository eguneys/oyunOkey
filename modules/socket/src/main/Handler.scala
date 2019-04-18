package oyun.socket

import akka.actor._
import akka.pattern.{ ask, pipe }

import play.api.libs.json._
import play.api.libs.iteratee.{ Iteratee, Enumerator }

import actorApi._
import oyun.hub.Trouper
import makeTimeout.large

object Handler {
  type Controller = PartialFunction[(String, JsObject), Unit]
  type Connecter = PartialFunction[Any, (Controller, JsEnumerator, SocketMember)]

  val emptyController: Controller = PartialFunction.empty

  // def apply(
  //   socket: SocketTrouper[_],
  //   uid: String,
  //   join: Any)(connecter: Connecter): Fu[JsSocketHandler] = {

  //   def baseController(member: SocketMember): Controller = {
  //     case ("p", _) => socket ! Ping(uid)
  //   }


  //   def iteratee(controller: Controller, member: SocketMember): JsIteratee = {
  //     val control = controller orElse baseController(member)
  //     Iteratee.foreach[JsValue](jsv =>
  //       jsv.asOpt[JsObject] foreach { obj =>
  //         obj str "t" foreach { t =>
  //           control.lift(t -> obj)
  //         }
  //       }
  //     ).map(_ => socket ! Quit(uid))
  //   }

  //   socket ? join map connecter map {
  //     case (controller, enum, member) => iteratee(controller, member) -> enum
  //   }
  // }

  type OnPing = (SocketTrouper[_], SocketMember, Socket.Uid) => Unit

  val defaultOnPing: OnPing = (socket, member, uid) => {
    socket setAlive uid
    member push {
      Socket.initialPong
    }
  }

  def iteratee(hub: oyun.hub.Env,
    controller: Controller,
    member: SocketMember,
    socket: SocketTrouper[_],
    uid: Socket.Uid,
    onPing: OnPing = defaultOnPing): JsIteratee = {

    val fullCtrl = controller orElse baseController(hub, socket, member, uid, onPing)
    Iteratee.foreach[JsValue] {
      case JsNull => onPing(socket, member, uid)
      case jsv => for {
        obj <- jsv.asOpt[JsObject]
        t <- (obj \ "t").asOpt[String]
      } fullCtrl(t -> obj)
    }

    .map(_ => socket ! Quit(uid))
  }

  private def baseController(
    hub: oyun.hub.Env,
    socket: SocketTrouper[_],
    member: SocketMember,
    uid: Socket.Uid,
    onPing: OnPing): Controller = {

    case ("p", o) =>
      onPing(socket, member, uid)

  }
}
