package oyun.game

import akka.actor._
import scalaz.{ NonEmptyList }

import oyun.common.Captcha
import oyun.hub.actorApi.captcha._

private final class Captcher extends Actor {

  def receive = {

    case AnyCaptcha =>
    // case GetCaptcha(id: String) => Impl get id pipeTo sender
    // case actorApi.NewCaptcha

    case ValidCaptcha(id: String, solution: String) =>

  }

  private object Impl {

    // def current = challenges.head

    // private var challenges: NonEmptyList[Captcha] = NonEmptyList(Captcha.default)
    
  }


}
