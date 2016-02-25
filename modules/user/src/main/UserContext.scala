package oyun.user

import play.api.mvc.{ Request, RequestHeader }

sealed trait UserContext {
  val req: RequestHeader
}

sealed abstract class BaseUserContext(val req: RequestHeader) extends UserContext {
}

final class BodyUserContext[A](val body: Request[A]) extends BaseUserContext(body)

object UserContext {
  def apply[A](req: Request[A]): BodyUserContext[A] =
    new BodyUserContext(req)
}
