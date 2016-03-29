// Generated with bin/trans-dump at 2016-03-29 10:07:11 UTC
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
  val `viewMasa` = new Key("viewMasa")
  val `join` = new Key("join")
  val `withdraw` = new Key("withdraw")
  val `finished` = new Key("finished")
  val `freeOnlineOkey` = new Key("freeOnlineOkey")
  val `chatRoom` = new Key("chatRoom")
  val `toggleTheChat` = new Key("toggleTheChat")
  val `talkInChat` = new Key("talkInChat")

  def keys = List(`createAGame`, `viewMasa`, `join`, `withdraw`, `finished`, `freeOnlineOkey`, `chatRoom`, `toggleTheChat`, `talkInChat`)

  lazy val count = keys.size
}
