package oyun.socket

import akka.actor._
import akka.pattern.{ ask, pipe }

import play.api.libs.json._
import play.api.libs.iteratee.{ Iteratee, Enumerator }

import actorApi._
import oyun.common.PimpedJson._
import makeTimeout.large

object Handler {
  type Controller = PartialFunction[(String, JsObject), Unit]
  type Connecter = PartialFunction[Any, (Controller, JsEnumerator, SocketMember)]

  def apply(
    socket: ActorRef,
    uid: String,
    join: Any)(connecter: Connecter): Fu[JsSocketHandler] = {

    def baseController(member: SocketMember): Controller = {
      case ("p", _) => socket ! Ping(uid)
    }


    def iteratee(controller: Controller, member: SocketMember): JsIteratee = {
      val control = controller orElse baseController(member)
      Iteratee.foreach[JsValue](jsv =>
        jsv.asOpt[JsObject] foreach { obj =>
          obj str "t" foreach { t =>
            control.lift(t -> obj)
          }
        }
      ).map(_ => socket ! Quit(uid))
    }

    socket ? join map connecter map {
      case (controller, enum, member) => iteratee(controller, member) -> enum
    }
  }
}
