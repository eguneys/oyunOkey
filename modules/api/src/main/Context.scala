package oyun.api

import oyun.user.{ UserContext, HeaderUserContext, BodyUserContext }

case class PageData()

object PageData {

  val default = PageData()
  def anon = default

}


sealed trait Context extends oyun.user.UserContextWrapper {
  val userContext: UserContext
  val pageData: PageData

  def lang = userContext.lang
}

sealed abstract class BaseContext(
  val userContext: oyun.user.UserContext,
  val pageData: PageData) extends Context

final class BodyContext[A](
  val bodyContext: BodyUserContext[A],
  data: PageData) extends BaseContext(bodyContext, data) {
  def body = bodyContext.body
}

final class HeaderContext(
  headerContext: HeaderUserContext,
  data: PageData) extends BaseContext(headerContext, data)

object Context {
  def apply(userContext: HeaderUserContext, pageData: PageData): HeaderContext =
    new HeaderContext(userContext, pageData)

  def apply[A](userContext: BodyUserContext[A], pageData: PageData): BodyContext[A] =
    new BodyContext(userContext, pageData)
}
