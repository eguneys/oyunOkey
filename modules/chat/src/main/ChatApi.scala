package oyun.chat

import oyun.db.dsl._
import oyun.user.{ User, UserRepo }

import okey.Side

final class ChatApi(
  coll: Coll,
  flood: oyun.security.Flood,
  maxLinesPerChat: Int,
  oyunBus: oyun.common.Bus,
  netDomain: String) {

  import Chat.{ userChatBSONHandler, chatIdBSONHandler, classify }
  import Chat.BSONFields._

  object userChat {
    def findOption(chatId: ChatId): Fu[Option[UserChat]] =
      coll.find($doc("_id" -> chatId)).uno[UserChat]

    def find(chatId: ChatId): Fu[UserChat] =
      findOption(chatId) map (_ | Chat.makeUser(chatId))


    def write(chatId: Chat.Id, userId: String, text: String, public: Boolean): Funit =
      makeLine(userId, text) flatMap {
        _ ?? { line =>
          pushLine(chatId, line) >>-
          oyunBus.publish(actorApi.ChatLine(chatId, line), classify(chatId))
        }
      }

    def system(chatId: Chat.Id, text: String) = {
      val line = UserLine(systemUserId, Writer delocalize text, false)
      pushLine(chatId, line) inject line.some
    }

    private[ChatApi] def makeLine(userId: String, t1: String): Fu[Option[UserLine]] = UserRepo byId userId map { _ flatMap { user =>
      Writer cut t1 ifFalse user.disabled flatMap { t2 =>
        flood.allowMessage(user.id, t2) option
        UserLine(user.username, Writer preprocessUserInput t2, user.troll)
      }
    }
    }
  }

  object playerChat {

    def findOption(chatId: ChatId): Fu[Option[MixedChat]] =
      coll.find($doc("_id" -> chatId)).uno[MixedChat]

    def find(chatId: ChatId): Fu[MixedChat] =
      findOption(chatId) map (_ | Chat.makeMixed(chatId))

    def write(chatId: Chat.Id, side: Side, text: String): Fu[Option[Line]] =
      makeLine(chatId, side, text) ?? { line =>
        pushLine(chatId, line) inject line.some
      }

    private def makeLine(chatId: Chat.Id, side: Side, t1: String): Option[Line] =
      Writer cut t1 flatMap { t2 =>
        flood.allowMessage(s"$chatId/${side.letter}", t2) option
          PlayerLine(side, Writer preprocessUserInput t2)
      }
  }

  private def pushLine(chatId: Chat.Id, line: Line): Funit = coll.update(
    $doc("_id" -> chatId),
    $doc("$push" -> $doc(
      lines -> $doc(
        "$each" -> List(line),
        "$slice" -> -maxLinesPerChat)
    )),
    upsert = true).void

  private object Writer {
    import java.util.regex.Matcher.quoteReplacement
    import org.apache.commons.lang3.StringEscapeUtils.escapeHtml4

    def preprocessUserInput(in: String) = delocalize(noPrivateUrl(escapeHtml4(in)))

    def cut(text: String) = Some(text.trim take 140) filter { _.nonEmpty }
    val delocalize = new oyun.common.String.Delocalizer(netDomain)
    val domainRegex = netDomain.replace(".", """\.""")
    val gameUrlRegex = (domainRegex + """\b/([\w]{8})[\w]{4}\b""").r
    def noPrivateUrl(str: String): String =
      gameUrlRegex.replaceAllIn(str, m => quoteReplacement(netDomain + "/" + (m group 1)))
  }

}
