package oyun.forum

case class CategView(
  categ: Categ,
  lastPost: Option[(Topic, Post, Int)]) {

  def nbTopics = categ nbTopics
  def nbPosts = categ nbPosts
  def lastPostId = categ lastPostId
  def lastPostUserId = lastPost.map(_._2).flatMap(_.userId)

  def slug = categ.slug
  def name = categ.name
  def desc = categ.desc

}

case class TopicView(
  categ: Categ,
  topic: Topic,
  lastPost: Option[Post],
  lastPage: Int) {

  def updatedAt = topic updatedAt
  def nbPosts = topic nbPosts
  def nbReplies = topic nbReplies

  def lastPostId = topic lastPostId
  def lastPostUserId = lastPost.flatMap(_.userId)

  def id = topic.id
  def slug = topic.slug
  def name = topic.name
  def views = topic.views
  def createdAt = topic.createdAt
}

case class PostUrlData(categ: String, topic: String, page: Int, number: Int)
