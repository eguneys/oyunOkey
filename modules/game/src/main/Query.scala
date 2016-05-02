package oyun.game

import org.joda.time.DateTime

import oyun.db.dsl._

object Query {

  import Game.{ BSONFields => F }

  def nowPlaying(u: String) = $doc(F.playingUids -> u)


  def checkable = F.checkAt $lt DateTime.now

  def checkableOld = F.checkAt $lt DateTime.now.minusHours(1)

}
