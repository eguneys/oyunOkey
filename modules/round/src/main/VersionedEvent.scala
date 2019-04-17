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

  import oyun.db.BSON
  import reactivemongo.bson._

  implicit val versionedEventHandler = new BSON[VersionedEvent] {
    def reads(r: BSON.Reader) = VersionedEvent(
      version = r int "v",
      typ = r str "t",
      encoded = r.strO("d").map(Left.apply).getOrElse(Right(JsNull)),
      only = Side(r str "o"),
      owner = r boolD "ow",
      watcher = r boolD "r")

    def writes(w: BSON.Writer, o: VersionedEvent) = BSONDocument(
      "v" -> o.version,
      "t" -> o.typ,
      "d" -> (o.encoded match {
        case Left(s) => s.some
        case Right(JsNull) => none
        case Right(js) => Json.stringify(js).some
      }),
      "o" -> o.only.map(_.letter.toString),
      "ow" -> w.boolO(o.owner),
      "w" -> w.boolO(o.watcher))

  }
}
