package oyun.setup

import oyun.lobby.Hook
import oyun.user.User

case class HookConfig(ratingRange: Option[String]) {

  // def hook(uid: oyun.socket.Socket.Uid,
  //   user: Option[User],
  //   sid: Option[String]): Either[Hook

  def hook(
    uid: oyun.socket.Socket.Uid,
    user: Option[User],
    sid: Option[String]): Hook = Hook.make(
      uid = uid,
      sid = sid,
      user = user)
}
