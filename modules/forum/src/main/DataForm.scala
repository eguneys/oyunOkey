package oyun.forum

import play.api.data._
import play.api.data.Forms._

private[forum] final class DataForm(val captcher: akka.actor.ActorSelection) extends oyun.hub.CaptchedForm {

  import DataForm._

  val postMapping = mapping(
    "text" -> text(minLength = 3)
  )(PostData.apply)(PostData.unapply)
    // .verifying(captchaFailMessage, validateCaptcha _)

  val post = Form(postMapping)

  def postWithCaptcha = withCaptcha(post)

  val topic = Form(mapping(
    "name" -> text(minLength = 3, maxLength = 100),
    "post" -> postMapping
  )(TopicData.apply)(TopicData.unapply))

}

object DataForm {

  case class PostData(
    text: String
  )

  case class TopicData(
    name: String,
    post: PostData
  ) {
  }

}
