package oyun.hub

import akka.pattern.ask
import play.api.data._

import actorApi.captcha._
import oyun.common.Captcha

trait CaptchedForm {

  import makeTimeout.large

  type CaptchedData = {
    def id: String
    def move: String
  }

  def captcher: akka.actor.ActorSelection

  def anyCaptcha: Fu[Captcha] =
    (captcher ? AnyCaptcha).mapTo[Captcha]

  def getCaptcha(id: String): Fu[Captcha] =
    (captcher ? GetCaptcha(id)).mapTo[Captcha]

  def withCaptcha[A](form: Form[A]): Fu[(Form[A], Captcha)] =
    anyCaptcha map (form -> _)

  def validateCaptcha(data: CaptchedData) =
    getCaptcha(data.id) awaitSeconds 2 valid data.move.trim.toLowerCase


  def captchaFailMessage = Captcha.failMessage
}
