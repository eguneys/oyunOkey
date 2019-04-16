package oyun.forum

import oyun.user.User
import org.joda.time.DateTime
import ornicar.scalalib.Random
import scala.concurrent.duration._

case class Post(
  _id: String,
  topicId: String,
  categId: String,
  author: Option[String],
  userId: Option[String],
  text: String,
  number: Int,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None,
  erasedAt: Option[DateTime] = None) {

  def id = _id

}

object Post {

  type ID = String

  val idSize = 8

  def make(
    topicId: String,
    categId: String,
    author: Option[String],
    userId: Option[String],
    text: String,
    number: Int): Post = {
    Post(_id = Random nextString idSize,
      topicId = topicId,
      author = author,
      userId = userId,
      text = text,
      number = number,
      createdAt = DateTime.now,
      categId = categId)
  }

}
