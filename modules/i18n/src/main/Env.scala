package oyun.i18n

import com.typesafe.config.Config
import play.api.i18n.Lang

final class Env(
  config: Config,
  messages: Messages) {

  private val settings = new {
    val WebPathRelative = config getString "web_path.relative"
    val FilePathRelative = config getString "file_path.relative"
  }
  import settings._

  lazy val pool = new I18nPool(
    langs = Lang.availables(play.api.Play.current).toSet,
    default = I18nKey.en)


  lazy val translator = new Translator(
    messages = messages,
    pool = pool)

  lazy val keys = new I18nKeys(translator)
}

object Env {
  import oyun.common.PlayApp

  lazy val current = "i18n" boot new Env(
    config = oyun.common.PlayApp loadConfig "i18n",
    messages = PlayApp.messages
  )
}
