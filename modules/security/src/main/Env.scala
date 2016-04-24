package oyun.security

import akka.actor.{ ActorRef, ActorSystem }
import com.typesafe.config.Config

import oyun.common.PimpedConfig._

final class Env(
  config: Config,
  system: ActorSystem,
  scheduler: oyun.common.Scheduler,
  db: oyun.db.Env) {

  private val settings = new {
    val CollectionSecurity = config getString "collection.security"
    val DisposableEmailProviderUrl = config getString "disposable_email.provider_url"
    val RecaptchaPrivateKey = config getString "recaptcha.private_key"
    val RecaptchaEndpoint = config getString "recaptcha.endpoint"
    val RecaptchaEnabled = config getBoolean "recaptcha.enabled"
  }
  import settings._

  val RecaptchaPublicKey = config getString "recaptcha.public_key"

  lazy val recaptcha: Recaptcha =
    if (RecaptchaEnabled) new RecaptchaGoogle(
      privateKey = RecaptchaPrivateKey,
      endpoint = RecaptchaEndpoint)
    else RecaptchaSkip

  lazy val forms = new DataForm(emailAddress)

  lazy val emailConfirm: EmailConfirm =
    // if (EmailConfirmEnabled) new EmailConfirmMailGun(
    //   )
    EmailConfirmSkip

  lazy val emailAddress = new EmailAddress(disposableEmailDomain)

  private lazy val disposableEmailDomain = new DisposableEmailDomain(
    providerUrl = DisposableEmailProviderUrl,
    busOption = system.oyunBus.some)

  lazy val api = new Api(emailAddress)


  private[security] lazy val storeColl = db(CollectionSecurity)
}

object Env {
  lazy val current = "security" boot new Env(
    config = oyun.common.PlayApp loadConfig "security",
    db = oyun.db.Env.current,
    system = oyun.common.PlayApp.system,
    scheduler = oyun.common.PlayApp.scheduler)
}
