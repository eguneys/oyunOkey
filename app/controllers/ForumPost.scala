package controllers

import oyun.app._
import oyun.common.{ HTTPRequest }
import views._

object ForumPost extends OyunController {

  def search(text: String, page: Int) = OpenBody { implicit ctx =>
    if (text.trim.isEmpty) Redirect(routes.ForumCateg.index).fuccess
    else fuccess(Ok(""))
  }

}
