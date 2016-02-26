package controllers

import oyun.user.UserContext

import play.api.mvc.RequestHeader

trait RequestGetter {
  protected def get(name: String)(implicit ctx: UserContext): Option[String] = get(name, ctx.req)

  protected def get(name: String, req: RequestHeader): Option[String] =
    req.queryString get name flatMap (_.headOption) filter (_.nonEmpty)
}
