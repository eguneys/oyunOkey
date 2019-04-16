package oyun.user

import play.api.mvc.{ Request, RequestHeader }

import oyun.common.Lang

sealed trait UserContext {

  val req: RequestHeader

  val me: Option[User]

  def lang: Lang

  def isAuth = me.isDefined

  def isAnon = !isAuth

  def is(user: User): Boolean = me ?? (user ==)
  
  def userId = me map (_.id)
}

sealed abstract class BaseUserContext(val req: RequestHeader, val me: Option[User], val lang: Lang) extends UserContext {
}

final class BodyUserContext[A](val body: Request[A], m: Option[User], l: Lang) extends BaseUserContext(body, m, l)

final class HeaderUserContext(r: RequestHeader, m: Option[User], l: Lang)
    extends BaseUserContext(r, m, l)

trait UserContextWrapper extends UserContext {
  val userContext: UserContext
  val req = userContext.req
  val me = userContext.me
}

object UserContext {
  def apply(req: RequestHeader, me: Option[User], lang: Lang): HeaderUserContext =
    new HeaderUserContext(req, me, lang)

  def apply[A](req: Request[A], me: Option[User], lang: Lang): BodyUserContext[A] =
    new BodyUserContext(req, me, lang)
}
