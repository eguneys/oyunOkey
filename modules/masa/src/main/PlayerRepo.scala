package oyun.masa

import reactivemongo.bson._

import okey.Side

import BSONHandlers._

object PlayerRepo {
  private lazy val coll = Env.current.playerColl

  private def selectMasa(masaId: String) = BSONDocument("mid" -> masaId) 

  def find(masaId: String): Fu[Option[Player]] =
    coll.find(selectMasa(masaId)).one[Player]

  def join(masaId: String, player: PlayerRef, side: Side) =
    find(masaId) flatMap {
      case _ => coll.insert(Player.make(masaId))
    } void
}
