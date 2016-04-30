package oyun.game

import oyun.db.dsl._

object Query {

  import Game.{ BSONFields => F }

  def nowPlaying(u: String) = $doc(F.playingUids -> u)

}
