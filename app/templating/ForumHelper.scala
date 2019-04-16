package oyun.app
package templating

import play.twirl.api.Html

import oyun.forum.Post

trait ForumHelper { self: UserHelper with StringHelper =>

  def authorName(post: Post) = post.userId match {
    case Some(userId) => userIdSpanMini(userId, withOnline = true)
    case None => Html(oyun.user.User.anonymous)
  }

  def authorLink(post: Post,
    cssClass: Option[String] = None,
    withOnline: Boolean = true) =
    post.userId.fold(Html(oyun.user.User.anonymous)) { userId =>
      userIdLink(userId.some, cssClass = cssClass, withOnline = withOnline)
    }

}
