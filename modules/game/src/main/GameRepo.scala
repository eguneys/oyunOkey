package oyun.game

import okey.{ Side, Sides, Status, EndScoreSheet }

import reactivemongo.bson.{ BSONDocument }
import oyun.db.dsl._

object GameRepo {
  // dirty ??
  private val coll = Env.current.gameColl

  type ID = String

  import BSONHandlers._
  import Game.{ BSONFields => F }

  def game(gameId: ID): Fu[Option[Game]] = coll.byId[Game](gameId)

  def pov(playerRef: PlayerRef): Fu[Option[Pov]] =
    coll.byId[Game](playerRef.gameId) map { gameOption =>
      gameOption flatMap { game =>
        game player playerRef.playerId map { Pov(game, _) }
      }
    }

  def pov(gameId: ID, side: Side): Fu[Option[Pov]] =
    game(gameId) map2 { (game: Game) => Pov(game, game player side) }

  def pov(gameId: ID, side: String): Fu[Option[Pov]] =
    Side(side) ?? (pov(gameId, _))

  def pov(fullId: ID): Fu[Option[Pov]] = pov(PlayerRef(fullId))

  def save(progress: Progress): Funit =
    GameDiff(progress.origin, progress.game) match {
      case (Nil, Nil) => funit
      case (sets, unsets) =>
        coll.update(
          $id(progress.origin.id),
          nonEmptyMod("$set", $doc(sets)) ++ nonEmptyMod("$unset", $doc(unsets))
        ).void
    }

  private def nonEmptyMod(mod: String, doc: BSONDocument) =
    if (doc.isEmpty) $empty else $doc(mod -> doc)


  def finish(
    id: ID,
    status: Status,
    result: Option[Sides[EndScoreSheet]]) = {
    import BSONHandlers.scoresBSONHandler

    val partialUnsets = $doc(
    )

    val unsets = 
      if (status >= Status.End) partialUnsets ++ $doc(F.checkAt -> true)
      else partialUnsets

    coll.update(
      $id(id),
      nonEmptyMod("$set", $doc(
        F.endScores -> result.map (BSONHandlers.sidesBSONHandler[EndScoreSheet].write _)
      )) ++ $doc("$unset" -> unsets)
    )
  }

  def insertDenormalized(g: Game): Funit = {
    val bson = (gameBSONHandler write g)
    coll insert bson void
  }
}
