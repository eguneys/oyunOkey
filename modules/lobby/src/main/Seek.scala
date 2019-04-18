package oyun.lobby

import org.joda.time.DateTime
import play.api.libs.json._

import oyun.game.PerfPicker

import oyun.user.User


case class Seek(
  _id: String,
  variant: Int,
  mode: Int,
  side: String,
  user: LobbyUser,
  ratingRange: String,
  createdAt: DateTime) {

  def id = _id

  def perf = perfType map user.perfAt
  
  def rating = perf.map(_.rating)

  def realVariant = okey.variant.Variant orDefault variant


  lazy val render: JsObject = Json.obj(
    "id" -> _id,
    "username" -> user.username,
    "rating" -> rating,
    "variant" -> Json.obj(
      "key" -> realVariant.key,
      "short" -> realVariant.shortName,
      "name" -> realVariant.name
    ),
    "side" -> okey.Side(side).??(_.name),
    "perf" -> Json.obj(
      "icon" -> perfType.map(_.iconChar.toString),
      "name" -> perfType.map(_.name)
    )
  )

  lazy val perfType = PerfPicker.perfType(realVariant)
}

object Seek {

  import reactivemongo.bson. { MapReader => _, MapWriter => _, _ }
  import oyun.db.BSON.MapValue.MapHandler
  import oyun.db.BSON.BSONJodaDateTimeHandler

  implicit val lobbyPerfBSONHandler = new BSONHandler[BSONInteger, LobbyPerf] {
    def read(b: BSONInteger) = LobbyPerf(b.value.abs)
    def write(x: LobbyPerf) = BSONInteger(x.rating)
  }

  private[lobby] implicit val lobbyUserBSONHandler = Macros.handler[LobbyUser]
  private[lobby] implicit val seekBSONHandler = Macros.handler[Seek]

}
