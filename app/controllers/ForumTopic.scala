package controllers

import oyun.app._
import oyun.common.{ HTTPRequest }
import oyun.forum.CategRepo
import play.api.libs.json._
import views._

object ForumTopic extends OyunController with ForumController {

  def form(categSlug: String) = Open { implicit ctx =>
    OptionFuOk(CategRepo bySlug categSlug) { categ =>
      forms.anyCaptcha map { html.forum.topic.form(categ, forms.topic, _) }
    }
  }

  def create(categSlug: String) = OpenBody { implicit ctx =>
    implicit val req = ctx.body
    OptionFuResult(CategRepo bySlug categSlug) { categ =>
      forms.topic.bindFromRequest.fold(
        err => forms.anyCaptcha.map { captcha =>
          BadRequest(html.forum.topic.form(categ, err, captcha))
        },
        data => topicApi.makeTopic(categ, data) map { topic =>
          Redirect(routes.ForumTopic.show(categSlug, topic.slug, 1))
        }
      )
    }
  }

  def show(categSlug: String, slug: String, page: Int) = Open { implicit ctx =>
    OptionFuOk(topicApi.show(categSlug, slug, page)) {
      case (categ, topic, posts) => for {
        form <- (!posts.hasNextPage) ?? forms.postWithCaptcha.map(_.some)
      } yield html.forum.topic.show(categ, topic, posts, form)
    }
  }

}
