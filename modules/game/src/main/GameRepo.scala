package oyun.game

import okey.{ Side, Sides, Status, EndScoreSheet }

import reactivemongo.bson.{ BSONDocument }
import oyun.db.dsl._

import oyun.user.{ User }

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

  def urgentGames(user: User): Fu[List[Pov]] =
    coll.list[Game](Query nowPlaying user.id, 100) map { games =>
      val povs = games flatMap { Pov(_, user) }
        povs sortBy (-_.game.updatedAtOrCreatedAt.getSeconds)
      // try {
      //povs sortWith Pov.priority
      // } catch {
      //   case e: IllegalArgumentException =>
      //     povs sortBy 
      // }
    }

  private def nonEmptyMod(mod: String, doc: BSONDocument) =
    if (doc.isEmpty) $empty else $doc(mod -> doc)


  def finish(
    id: ID,
    status: Status,
    result: Option[Sides[EndScoreSheet]],
    winnerSide: Option[Side],
    winnerId: Option[String]) = {
    import BSONHandlers.scoresBSONHandler

    val partialUnsets = $doc(
      F.playingUids -> true
    )

    val unsets = 
      if (status >= Status.NormalEnd) partialUnsets ++ $doc(F.checkAt -> true)
      else partialUnsets

    coll.update(
      $id(id),
      nonEmptyMod("$set", $doc(
        F.winnerId -> winnerId,
        F.winnerSide -> winnerSide.map(_.letter.toString),
        F.endScores -> result.map (BSONHandlers.sidesBSONHandler[EndScoreSheet].write _)
      )) ++ $doc("$unset" -> unsets)
    )
  }

  def insertDenormalized(g: Game): Funit = {
    val userIds = g.userIds.distinct

    val bson = (gameBSONHandler write g) ++ $doc(
      F.playingUids -> (g.started && userIds.nonEmpty).option(userIds)
    )
    coll insert bson void
  }
}
