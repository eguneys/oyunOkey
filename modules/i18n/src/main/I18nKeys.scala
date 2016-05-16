// Generated with bin/trans-dump at 2016-05-16 15:59:15 UTC
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
  val `backToMasa` = new Key("backToMasa")
  val `theBestFreeOkeyServer` = new Key("theBestFreeOkeyServer")
  val `freeOnlineOkeyGamePlayOkeyNowInACleanInterfaceNoRegistrationNoAdsNoPluginRequiredPlayOkeyWithComputerFriendsOrRandomOpponents` = new Key("freeOnlineOkeyGamePlayOkeyNowInACleanInterfaceNoRegistrationNoAdsNoPluginRequiredPlayOkeyWithComputerFriendsOrRandomOpponents")
  val `masaFAQ` = new Key("masaFAQ")
  val `join` = new Key("join")
  val `invite` = new Key("invite")
  val `withdraw` = new Key("withdraw")
  val `inProgress` = new Key("inProgress")
  val `play` = new Key("play")
  val `playingRightNow` = new Key("playingRightNow")
  val `playing` = new Key("playing")
  val `finished` = new Key("finished")
  val `players` = new Key("players")
  val `games` = new Key("games")
  val `gamesPlayed` = new Key("gamesPlayed")
  val `nbWins` = new Key("nbWins")
  val `nbLoss` = new Key("nbLoss")
  val `aiBot` = new Key("aiBot")
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
  val `yuzbirOkey` = new Key("yuzbirOkey")
  val `roundsToPlay` = new Key("roundsToPlay")
  val `rounds` = new Key("rounds")
  val `roundX` = new Key("roundX")
  val `seeAllMasas` = new Key("seeAllMasas")
  val `openMasas` = new Key("openMasas")
  val `masas` = new Key("masas")
  val `more` = new Key("more")
  val `rated` = new Key("rated")
  val `casual` = new Key("casual")
  val `leaderboard` = new Key("leaderboard")
  val `oyunkeyfMasas` = new Key("oyunkeyfMasas")
  val `masaNotFound` = new Key("masaNotFound")
  val `masaDoesNotExist` = new Key("masaDoesNotExist")
  val `masaMayHaveBeenCancelled` = new Key("masaMayHaveBeenCancelled")
  val `masaTryRefreshingPage` = new Key("masaTryRefreshingPage")
  val `returnToLobby` = new Key("returnToLobby")
  val `returnToMasasHomepage` = new Key("returnToMasasHomepage")
  val `youArePlaying` = new Key("youArePlaying")
  val `joinTheGame` = new Key("joinTheGame")
  val `playerHasJoinedTheGame` = new Key("playerHasJoinedTheGame")
  val `playerHasLeftTheGame` = new Key("playerHasLeftTheGame")
  val `thereIsAGameInProgress` = new Key("thereIsAGameInProgress")
  val `yourTurn` = new Key("yourTurn")
  val `waitingForOpponent` = new Key("waitingForOpponent")
  val `gameOver` = new Key("gameOver")
  val `gameEnded` = new Key("gameEnded")
  val `gameAborted` = new Key("gameAborted")
  val `gameFinished` = new Key("gameFinished")
  val `gameMiddleFinished` = new Key("gameMiddleFinished")
  val `gameEndBy` = new Key("gameEndBy")
  val `openSeries` = new Key("openSeries")
  val `openPairs` = new Key("openPairs")
  val `collectOpen` = new Key("collectOpen")
  val `leaveTaken` = new Key("leaveTaken")
  val `scores` = new Key("scores")
  val `replay` = new Key("replay")
  val `waitingPlayers` = new Key("waitingPlayers")
  val `gameEndByHand` = new Key("gameEndByHand")
  val `gameEndByPair` = new Key("gameEndByPair")
  val `gameEndByDiscardOkey` = new Key("gameEndByDiscardOkey")
  val `handZero` = new Key("handZero")
  val `handOkeyLeft` = new Key("handOkeyLeft")
  val `handNotOpened` = new Key("handNotOpened")
  val `handOpenedPair` = new Key("handOpenedPair")
  val `handOpenedSome` = new Key("handOpenedSome")
  val `reconnecting` = new Key("reconnecting")
  val `signIn` = new Key("signIn")
  val `signUp` = new Key("signUp")
  val `community` = new Key("community")
  val `contact` = new Key("contact")
  val `forum` = new Key("forum")
  val `questionsAndAnswers` = new Key("questionsAndAnswers")
  val `inbox` = new Key("inbox")
  val `preferences` = new Key("preferences")
  val `profile` = new Key("profile")
  val `logOut` = new Key("logOut")
  val `online` = new Key("online")
  val `offline` = new Key("offline")
  val `networkLagBetweenYouAndOyunkeyf` = new Key("networkLagBetweenYouAndOyunkeyf")
  val `timeToProcessAMoveOnOyunkeyfServer` = new Key("timeToProcessAMoveOnOyunkeyfServer")
  val `newToOyunkeyf` = new Key("newToOyunkeyf")
  val `forgotPassword` = new Key("forgotPassword")
  val `passwordReset` = new Key("passwordReset")
  val `email` = new Key("email")
  val `password` = new Key("password")
  val `username` = new Key("username")
  val `usernameOrEmail` = new Key("usernameOrEmail")
  val `textIsTooShort` = new Key("textIsTooShort")
  val `textIsTooLong` = new Key("textIsTooLong")
  val `invalidCaptcha` = new Key("invalidCaptcha")
  val `haveAnAccount` = new Key("haveAnAccount")
  val `changePassword` = new Key("changePassword")
  val `changeEmail` = new Key("changeEmail")
  val `checkYourEmail` = new Key("checkYourEmail")
  val `termsOfService` = new Key("termsOfService")
  val `youNeedAnAccountToDoThat` = new Key("youNeedAnAccountToDoThat")
  val `computersAreNotAllowedToPlay` = new Key("computersAreNotAllowedToPlay")
  val `byRegisteringYouAgreeToBeBoundByOur` = new Key("byRegisteringYouAgreeToBeBoundByOur")
  val `weHaveSentYouAnEmailClickTheLink` = new Key("weHaveSentYouAnEmailClickTheLink")
  val `ifYouDoNotSeeTheEmailCheckOtherPlaces` = new Key("ifYouDoNotSeeTheEmailCheckOtherPlaces")
  val `memberSince` = new Key("memberSince")
  val `membersOnly` = new Key("membersOnly")
  val `lastSeenActive` = new Key("lastSeenActive")
  val `tpTimeSpentPlaying` = new Key("tpTimeSpentPlaying")
  val `nbConnectedPlayers` = new Key("nbConnectedPlayers")
  val `nbGamesInPlay` = new Key("nbGamesInPlay")
  val `isOyunkeyfLagging` = new Key("isOyunkeyfLagging")
  val `playOkeyEverywhere` = new Key("playOkeyEverywhere")

  def keys = List(`createAGame`, `createAMasa`, `viewMasa`, `backToMasa`, `theBestFreeOkeyServer`, `freeOnlineOkeyGamePlayOkeyNowInACleanInterfaceNoRegistrationNoAdsNoPluginRequiredPlayOkeyWithComputerFriendsOrRandomOpponents`, `masaFAQ`, `join`, `invite`, `withdraw`, `inProgress`, `play`, `playingRightNow`, `playing`, `finished`, `players`, `games`, `gamesPlayed`, `nbWins`, `nbLoss`, `aiBot`, `freeOnlineOkey`, `chatRoom`, `toggleTheChat`, `talkInChat`, `createdBy`, `by`, `winner`, `cancel`, `variant`, `standard`, `yuzbirOkey`, `roundsToPlay`, `rounds`, `roundX`, `seeAllMasas`, `openMasas`, `masas`, `more`, `rated`, `casual`, `leaderboard`, `oyunkeyfMasas`, `masaNotFound`, `masaDoesNotExist`, `masaMayHaveBeenCancelled`, `masaTryRefreshingPage`, `returnToLobby`, `returnToMasasHomepage`, `youArePlaying`, `joinTheGame`, `playerHasJoinedTheGame`, `playerHasLeftTheGame`, `thereIsAGameInProgress`, `yourTurn`, `waitingForOpponent`, `gameOver`, `gameEnded`, `gameAborted`, `gameFinished`, `gameMiddleFinished`, `gameEndBy`, `openSeries`, `openPairs`, `collectOpen`, `leaveTaken`, `scores`, `replay`, `waitingPlayers`, `gameEndByHand`, `gameEndByPair`, `gameEndByDiscardOkey`, `handZero`, `handOkeyLeft`, `handNotOpened`, `handOpenedPair`, `handOpenedSome`, `reconnecting`, `signIn`, `signUp`, `community`, `contact`, `forum`, `questionsAndAnswers`, `inbox`, `preferences`, `profile`, `logOut`, `online`, `offline`, `networkLagBetweenYouAndOyunkeyf`, `timeToProcessAMoveOnOyunkeyfServer`, `newToOyunkeyf`, `forgotPassword`, `passwordReset`, `email`, `password`, `username`, `usernameOrEmail`, `textIsTooShort`, `textIsTooLong`, `invalidCaptcha`, `haveAnAccount`, `changePassword`, `changeEmail`, `checkYourEmail`, `termsOfService`, `youNeedAnAccountToDoThat`, `computersAreNotAllowedToPlay`, `byRegisteringYouAgreeToBeBoundByOur`, `weHaveSentYouAnEmailClickTheLink`, `ifYouDoNotSeeTheEmailCheckOtherPlaces`, `memberSince`, `membersOnly`, `lastSeenActive`, `tpTimeSpentPlaying`, `nbConnectedPlayers`, `nbGamesInPlay`, `isOyunkeyfLagging`, `playOkeyEverywhere`)

  lazy val count = keys.size
}
