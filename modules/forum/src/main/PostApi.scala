package oyun.forum

import oyun.common.paginator._
import oyun.db.dsl._
import oyun.db.paginator._

final class PostApi(
  env: Env,
  maxPerPage: oyun.common.MaxPerPage,
  bus: oyun.common.Bus
) {

  import BSONHandlers._

  def get(postId: String): Fu[Option[(Topic, Post)]] = {
    for {
      post <- optionT(env.postColl.byId[Post](postId))
      topic <- optionT(env.topicColl.byId[Topic](post.topicId))
    } yield topic -> post
  } run
  

  def lastPageOf(topic: Topic) =
    math.ceil(topic.nbPosts / maxPerPage.value.toFloat).toInt

  def paginator(topic: Topic, page: Int): Fu[Paginator[Post]] = Paginator(
    new Adapter(
      collection = env.postColl,
      selector = PostRepo() selectTopic topic.id,
      projection = $empty,
      sort = PostRepo.sortQuery
    ),
    currentPage = page,
    maxPerPage = maxPerPage
  )
}
