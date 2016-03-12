package controllers

import oyun.app._
import oyun.api.Context
import oyun.masa.{ PlayerRepo, AnonCookie }

private[controllers] trait TheftPrevention { self: OyunController =>


  protected def playerForReq(masaId: String)(implicit ctx: Context) =
    ctx.userId match {
      case Some(userId) => PlayerRepo.findByUserId(masaId, userId)
      case None =>
        ctx.req.cookies.get(AnonCookie.name).map(_.value) match {
          case Some(playerId) =>
            PlayerRepo.find(masaId, playerId).map(_.filterNot(_.hasUser))
          case None => fuccess(None)
        }
    }
}
