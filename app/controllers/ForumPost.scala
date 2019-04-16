package controllers

import oyun.app._
import oyun.common.{ HTTPRequest }
import views._

object ForumPost extends OyunController with ForumController {

  def search(text: String, page: Int) = OpenBody { implicit ctx =>
    if (text.trim.isEmpty) Redirect(routes.ForumCateg.index).fuccess
    else fuccess(Ok(""))
  }

  def create(categSlug: String, slug: String, page: Int) = OpenBody { implicit ctx =>

    implicit val req = ctx.body
    OptionFuResult(topicApi.show(categSlug, slug, page)) {
      case (categ, topic, posts) =>
        forms.post.bindFromRequest.fold(
          err => fuccess(BadRequest(html.forum.topic.show(categ, topic, posts, err.some))),
          data => postApi.makePost(categ, topic, data) map { post =>
            Redirect(routes.ForumPost.redirect(post.id))
          }
        )
    }
  }

  def redirect(id: String) = Open { implicit ctx =>
    OptionFuResult(postApi.urlData(id)) {
      case oyun.forum.PostUrlData(categ, topic, page, number) =>
        fuccess(Redirect(routes.ForumTopic.show(categ, topic, page).url + "#" + number))
    }
  }

}
