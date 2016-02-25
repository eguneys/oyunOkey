package oyun.api

import oyun.user.{ UserContext, BodyUserContext }

case class PageData()

object PageData {

  val default = PageData()
  def anon = default

}


sealed trait Context {
}

sealed abstract class BaseContext(
  val userContext: oyun.user.UserContext,
  val pageData: PageData) extends Context

final class BodyContext[A](
  val bodyContext: BodyUserContext[A],
  data: PageData) extends BaseContext(bodyContext, data) {
}

object Context {
  def apply[A](userContext: BodyUserContext[A], pageData: PageData): BodyContext[A] =
    new BodyContext(userContext, pageData)
}
