package controllers

import oyun.app._
import views._

object ForumCateg extends OyunController with ForumController {

  def index = Open { implicit ctx =>
    for {
      categs <- categApi.list()
      _ <- Env.user.lightUserApi preloadMany categs.flatMap(_.lastPostUserId)
    } yield html.forum.categ.index(categs)
  }

  def show(slug: String, page: Int) = Open { implicit ctx =>
    Reasonable(page, 50, errorPage = notFound) {
      OptionFuOk(categApi.show(slug, page)) {
        case (categ, topics) => for {
          _ <- Env.user.lightUserApi preloadMany topics.currentPageResults.flatMap(_.lastPostUserId)
        } yield html.forum.categ.show(categ, topics)
      }
    }
  }
  
}
