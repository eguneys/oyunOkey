package oyun.socket

import akka.actor._
import play.api.libs.json.JsObject
import scala.collection.mutable

import actorApi.{ SocketLeave, SocketEnter }
import oyun.hub.actorApi.{ SendTo, WithUserIds }

private final class UserRegister extends Actor {

  override def preStart() {
    context.system.oyunBus.subscribe(self, 'users, 'socketDoor)
  }

  override def postStop() {
    super.postStop()
    context.system.oyunBus.unsubscribe(self)
  }

  type UID = String
  type UserId = String

  val users = mutable.Map.empty[UserId, mutable.Map[UID, SocketMember]]

  def receive = {

    case SendTo(userId, msg) => sendTo(userId, msg)

    case WithUserIds(f) => f(users.keys)

    case SocketEnter(uid, member) => member.userId foreach { userId =>
      users get userId match {
        case None => users += (userId -> mutable.Map(uid -> member))
        case Some(members) => members += (uid -> member)
      }
    }

    case SocketLeave(uid, member) => member.userId foreach { userId =>
      users get userId foreach { members =>
        members -= uid
        if (members.isEmpty) users -= userId
      }
    }
  }

  private def sendTo(userId: String, msg: JsObject) {
    users get userId foreach { members =>
      members.values foreach (_ push msg)
    }
  }
}