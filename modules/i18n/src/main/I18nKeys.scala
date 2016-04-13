// Generated with bin/trans-dump at 2016-04-13 16:27:27 UTC
package oyun.i18n

import play.twirl.api.Html
import play.api.i18n.Lang

import oyun.user.UserContext

final class I18nKeys(translator: Translator) {

  final class Key(val key: String) extends I18nKey {

    def apply(args: Any*)(implicit ctx: UserContext): Html =
      translator.html(key, args.toList)(ctx.req)

    def str(args: Any*)(implicit ctx: UserContext): String =
      translator.str(key, args.toList)(ctx.req)

    def to(lang: Lang)(args: Any*): String =
      translator.transTo(key, args.toList)(lang)
  }

  def untranslated(message: String) = Untranslated(message)

  val `createAGame` = new Key("createAGame")
  val `createAMasa` = new Key("createAMasa")
  val `viewMasa` = new Key("viewMasa")
  val `join` = new Key("join")
  val `withdraw` = new Key("withdraw")
  val `finished` = new Key("finished")
  val `freeOnlineOkey` = new Key("freeOnlineOkey")
  val `chatRoom` = new Key("chatRoom")
  val `toggleTheChat` = new Key("toggleTheChat")
  val `talkInChat` = new Key("talkInChat")
  val `createdBy` = new Key("createdBy")
  val `by` = new Key("by")
  val `winner` = new Key("winner")
  val `cancel` = new Key("cancel")
  val `variant` = new Key("variant")
  val `standard` = new Key("standard")
  val `roundsToPlay` = new Key("roundsToPlay")
  val `rounds` = new Key("rounds")

  def keys = List(`createAGame`, `createAMasa`, `viewMasa`, `join`, `withdraw`, `finished`, `freeOnlineOkey`, `chatRoom`, `toggleTheChat`, `talkInChat`, `createdBy`, `by`, `winner`, `cancel`, `variant`, `standard`, `roundsToPlay`, `rounds`)

  lazy val count = keys.size
}
