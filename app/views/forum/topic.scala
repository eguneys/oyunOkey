package views.html
package forum

import play.api.data.Form

import oyun.api.Context
import oyun.app.templating.Environment._
import oyun.app.ui.ScalatagsTemplate._
import oyun.common.paginator.Paginator


import controllers.routes

object topic {

  def form(categ: oyun.forum.Categ, form: Form[_])(implicit ctx: Context) = ""


  def show(categ: oyun.forum.Categ, topic: oyun.forum.Topic, posts: Paginator[oyun.forum.Post], formWithCaptcha: Option[Form[_]])(implicit ctx: Context) = ""
  
}
