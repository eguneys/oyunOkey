package controllers

import play.api.mvc._

import oyun.api.Context
import oyun.app._
import oyun.forum

private[controllers] trait ForumController { self: OyunController =>

  protected def categApi = Env.forum.categApi
  protected def topicApi = Env.forum.topicApi
  protected def postApi = Env.forum.postApi
  protected def forms = Env.forum.forms

}
