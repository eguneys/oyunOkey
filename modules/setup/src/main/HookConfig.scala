package oyun.setup

import oyun.lobby.Hook
import oyun.user.User

case class HookConfig(ratingRange: Option[String]) {

  def hook(
    uid: String,
    user: Option[User],
    sid: Option[String]): Hook = Hook.make(
      uid = uid,
      sid = sid,
      user = user)
}
