package oyun.forum

import oyun.db.dsl._
import reactivemongo.bson._

private object BSONHandlers {

  implicit val CategBSONHandler = Macros.handler[Categ]

  implicit val PostBSONHandler = Macros.handler[Post]

  implicit val TopicBSONHandler = Macros.handler[Topic]

}
