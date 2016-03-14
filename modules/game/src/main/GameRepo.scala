package oyun.game

import okey.{ Side }

import oyun.db.api._

object GameRepo {

  import tube.gameTube

  type ID = String

  def game(gameId: ID): Fu[Option[Game]] = $find byId gameId

  def pov(playerRef: PlayerRef): Fu[Option[Pov]] =
    $find byId playerRef.gameId map { gameOption =>
      gameOption flatMap { game =>
        game player playerRef.playerId map { Pov(game, _) }
      }
    }

  def pov(gameId: ID, side: Side): Fu[Option[Pov]] =
    $find byId gameId map2 { (game: Game) => Pov(game, game player side) }

  def pov(gameId: ID, side: String): Fu[Option[Pov]] =
    Side(side) ?? (pov(gameId, _))

  def pov(fullId: ID): Fu[Option[Pov]] = pov(PlayerRef(fullId))


  def insertDenormalized(g: Game): Funit = {
    val bson = (gameTube.handler write g)
    $insert bson bson
  }
}
