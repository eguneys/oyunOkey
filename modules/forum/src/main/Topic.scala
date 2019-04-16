package oyun.forum

import org.joda.time.DateTime
import ornicar.scalalib.Random

case class Topic(
  _id: String,
  categId: String,
  slug: String,
  name: String,
  views: Int,
  createdAt: DateTime,
  updatedAt: DateTime,
  nbPosts: Int,
  lastPostId: String) {


  def id = _id

  def nbReplies: Int = nbPosts - 1

  def withPost(post: Post): Topic = copy(
    nbPosts = nbPosts + 1,
    lastPostId = post.id,
    updatedAt = post.createdAt
  )

}

object Topic {

  def nameToId(name: String) = (oyun.common.String slugify name) |> { slug =>
    if (slug.size > (name.size / 2)) slug else Random nextString 8
  }

  val idSize = 8

  def make(
    categId: String,
    slug: String,
    name: String): Topic = Topic(
    _id = Random nextString idSize,
      categId = categId,
      slug = slug,
      name = name,
      views = 0,
      createdAt = DateTime.now,
      updatedAt = DateTime.now,
      nbPosts = 0,
      lastPostId = "")

}
