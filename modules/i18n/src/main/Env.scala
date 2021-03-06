package oyun.i18n

import com.typesafe.config.Config
import play.api.i18n.Lang

final class Env(
  config: Config,
  appPath: String) {

  private val settings = new {
    val WebPathRelative = config getString "web_path.relative"
    val FilePathRelative = config getString "file_path.relative"
    val CdnDomain = config getString "cdn_domain"
  }
  import settings._

  // public settings
  val RequestHandlerProtocol = config getString "request_handler.protocol"

  // lazy val pool = new I18nPool(
  //   langs = Lang.availables(play.api.Play.current).toSet,
  //   default = I18nKey.en)


  // lazy val translator = new Translator(
  //   messages = messages,
  //   pool = pool)

  // lazy val keys = new I18nKeys(translator)

  // lazy val requestHandler = new I18nRequestHandler(
  //   pool,
  //   RequestHandlerProtocol,
  //   CdnDomain)

  lazy val jsDump = new JsDump(path = appPath + "/" + WebPathRelative)

  def cli = new oyun.common.Cli {
    def process = {
      case "i18n" :: "js" :: "dump" :: Nil =>
        jsDump.apply inject "Dumped JavaScript translations"
    }
  }
}

object Env {
  import oyun.common.PlayApp

  lazy val current = "i18n" boot new Env(
    config = oyun.common.PlayApp loadConfig "i18n",
    appPath = PlayApp withApp (_.path getCanonicalPath)
  )
}
