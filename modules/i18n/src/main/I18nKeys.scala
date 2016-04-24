// Generated with bin/trans-dump at 2016-04-24 12:31:59 UTC
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
  val `play` = new Key("play")
  val `playingRightNow` = new Key("playingRightNow")
  val `playing` = new Key("playing")
  val `finished` = new Key("finished")
  val `players` = new Key("players")
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
  val `seeAllMasas` = new Key("seeAllMasas")
  val `openMasas` = new Key("openMasas")
  val `masas` = new Key("masas")
  val `more` = new Key("more")
  val `rated` = new Key("rated")
  val `leaderboard` = new Key("leaderboard")
  val `oyunkeyfMasas` = new Key("oyunkeyfMasas")
  val `youArePlaying` = new Key("youArePlaying")
  val `joinTheGame` = new Key("joinTheGame")
  val `playerHasJoinedTheGame` = new Key("playerHasJoinedTheGame")
  val `playerHasLeftTheGame` = new Key("playerHasLeftTheGame")
  val `thereIsAGameInProgress` = new Key("thereIsAGameInProgress")
  val `yourTurn` = new Key("yourTurn")
  val `waitingForOpponent` = new Key("waitingForOpponent")
  val `gameOver` = new Key("gameOver")
  val `openSeries` = new Key("openSeries")
  val `openPairs` = new Key("openPairs")
  val `collectOpen` = new Key("collectOpen")
  val `scores` = new Key("scores")
  val `waitingPlayers` = new Key("waitingPlayers")

  def keys = List(`createAGame`, `createAMasa`, `viewMasa`, `join`, `withdraw`, `play`, `playingRightNow`, `playing`, `finished`, `players`, `freeOnlineOkey`, `chatRoom`, `toggleTheChat`, `talkInChat`, `createdBy`, `by`, `winner`, `cancel`, `variant`, `standard`, `roundsToPlay`, `rounds`, `seeAllMasas`, `openMasas`, `masas`, `more`, `rated`, `leaderboard`, `oyunkeyfMasas`, `youArePlaying`, `joinTheGame`, `playerHasJoinedTheGame`, `playerHasLeftTheGame`, `thereIsAGameInProgress`, `yourTurn`, `waitingForOpponent`, `gameOver`, `openSeries`, `openPairs`, `collectOpen`, `scores`, `waitingPlayers`)

  lazy val count = keys.size
}
