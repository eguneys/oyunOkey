package oyun.forum

case class Categ(
  _id: String, // slug
  name: String,
  desc: String,
  pos: Int,
  nbTopics: Int,
  nbPosts: Int,
  lastPostId: String) {

  def id = _id

  def slug = id

  def withTopic(post: Post): Categ = copy(
    nbTopics = nbTopics + 1,
    nbPosts = nbPosts + 1,
    lastPostId = post.id)

}
