package oyun.game

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

  def pov(fullId: ID): Fu[Option[Pov]] = pov(PlayerRef(fullId))


  def insertDenormalized(g: Game): Funit = {
    val bson = (gameTube.handler write g)
    $insert bson bson
  }
}
