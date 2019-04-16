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

  def lastByTopic(topic: Topic): Fu[Option[Post]] =
    coll.find(selectTopic(topic.id)).sort($sort.createdDesc).uno[Post]


  def countBeforeNumber(topicId: String, number: Int): Fu[Int] =
    coll.countSel(selectTopic(topicId) ++ $doc("number" -> $lt(number)))
}
