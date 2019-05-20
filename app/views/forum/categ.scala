package views.html
package forum

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.paginator.Paginator

import controllers.routes

object categ {

  def index(categs: List[oyun.forum.CategView])(implicit ctx: Context) = views.html.base.layout(
    title = trans.forum.txt(),
    moreCss = cssTag("forum"),
    openGraph = oyun.app.ui.OpenGraph(
      title = "Oyunkeyf topluluk forumu",
      url = s"$netBaseUrl${routes.ForumCateg.index.url}",
      description = "Okey tartışmaları ve oyunkeyf hakkında geri bildirim"
    ).some
  ) {
    
  }

  def show(categ: oyun.forum.Categ, topics: Paginator[oyun.forum.TopicView])(implicit ctx: Context) = ""
  
}
