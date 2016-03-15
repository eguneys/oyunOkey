package oyun.round

import play.api.libs.json._

import actorApi.Member
import okey.Side
import oyun.game.Event

case class VersionedEvent(
  version: Int,
  typ: String,
  encoded: Either[String, JsValue],
  only: Option[Side],
  owner: Boolean,
  watcher: Boolean) {

  lazy val decoded: JsValue = encoded match {
    case Left(s) => Json parse s
    case Right(js) => js
  }

  def jsFor(m: Member): JsObject = if (visibleBy(m)) {
    if (decoded == JsNull) Json.obj("v" -> version, "t" -> typ)
    else Json.obj("v" -> version, "t" -> typ, "d" -> decoded)
  }
  else Json.obj("v" -> version)


  private def visibleBy(m: Member): Boolean =
    if (watcher && m.owner) false
    else if (owner && m.watcher) false
    else only.fold(true)(_ == m.side)

  override def toString = s"Event $version $typ"
}

private[round] object VersionedEvent {

  def apply(e: Event, v: Int): VersionedEvent = VersionedEvent(
    version = v,
    typ = e.typ,
    encoded = Right(e.data),
    only = e.only,
    owner = e.owner,
    watcher = e.watcher)
}
