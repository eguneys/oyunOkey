package oyun.forum

import oyun.db.dsl._
import org.joda.time.DateTime

object PostRepo extends PostRepo() {
  def apply(): PostRepo = PostRepo
}

sealed abstract class PostRepo {

  import BSONHandlers.PostBSONHandler

  private val coll = Env.current.postColl

  def selectTopic(topicId: String) = $doc("topicId" -> topicId)

  def sortQuery = $sort.createdAsc

}
