package oyun.game

import ornicar.scalalib.Random

object IdGenerator {
  def game = Random nextString Game.gameIdSize

  def player = Random nextString Game.playerIdSize
}
