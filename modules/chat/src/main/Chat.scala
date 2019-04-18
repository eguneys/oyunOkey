package oyun.chat

import oyun.user.User

sealed trait AnyChat {
  def id: ChatId
  def lines: List[Line]

  def forUser(u: Option[User]): AnyChat

  def toJsonString = Line toJsonString lines
}

sealed trait Chat[L <: Line] extends AnyChat {
  def id: ChatId
  def lines: List[L]
  def nonEmpty = lines exists (_.isHuman)
}

case class UserChat(
  id: ChatId,
  lines: List[UserLine]) extends Chat[UserLine] {

  def forUser(u: Option[User]) = u.??(_.troll).fold(this,
    copy(lines = lines filterNot (_.troll)))
}

case class MixedChat(
  id: ChatId,
  lines: List[Line]) extends Chat[Line] {

  def forUser(u: Option[User]) = u.??(_.troll).fold(this,
    copy(lines = lines filter {
      case l: UserLine => !l.troll
      case l: PlayerLine => true
    }))
}

object Chat {

  case class Id(value: String) extends AnyVal with StringValue

  import oyun.db.BSON

  def makeUser(id: ChatId) = UserChat(id, Nil)
  def makeMixed(id: ChatId) = MixedChat(id, Nil)

  object BSONFields {
    val id = "_id"
    val lines = "l"
  }

  def classify(id: Chat.Id): Symbol = Symbol(s"chat:$id")

  import BSONFields._
  import reactivemongo.bson.BSONDocument

  implicit val mixedChatBSONHandler = new BSON[MixedChat] {
    implicit def lineHandler = Line.lineBSONHandler
    def reads(r: BSON.Reader): MixedChat = MixedChat(
      id = r str id,
      lines = r get[List[Line]](lines))
    def writes(w: BSON.Writer, o: MixedChat) = BSONDocument(
      id -> o.id,
      lines -> o.lines
    )
  }

  implicit val chatIdIso = oyun.common.Iso.string[Id](Id.apply, _.value)
  implicit val chatIdBSONHandler = oyun.db.BSON.stringIsoHandler(chatIdIso)

  implicit val userChatBSONHandler = new BSON[UserChat] {
    def reads(r: BSON.Reader): UserChat = UserChat(
      id = r str id,
      lines = r get[List[UserLine]](lines))
    def writes(w: BSON.Writer, o: UserChat) = BSONDocument(
      id -> o.id,
      lines -> o.lines
    )
  }
}
