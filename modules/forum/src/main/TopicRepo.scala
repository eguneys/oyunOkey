package oyun.forum

import oyun.db.dsl._

object TopicRepo extends TopicRepo() {

  def apply(): TopicRepo = TopicRepo

}

sealed abstract class TopicRepo() {

  import BSONHandlers.TopicBSONHandler

  private val coll = Env.current.topicColl

  def byTree(categSlug: String, slug: String): Fu[Option[Topic]] =
    coll.uno[Topic]($doc("categId" -> categSlug, "slug" -> slug))

  def nextSlug(categ: Categ, name: String, it: Int = 1): Fu[String] = {
    val slug = Topic.nameToId(name) + ~(it != 1).option("-" + it)
    fuccess(slug)
  }

  def incViews(topic: Topic) =
    coll.incFieldUnchecked($id(topic.id), "views")

  def byCategQuery(categ: Categ) = $doc("categId" -> categ.slug)

}
