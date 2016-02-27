package oyun.game

import ornicar.scalalib.Random

object IdGenerator {
  def game = Random nextStringUppercase Game.gameIdSize

  def player = Random nextStringUppercase Game.playerIdSize
}
