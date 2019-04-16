package oyun.forum

import oyun.common.paginator._
import oyun.db.dsl._
import oyun.db.paginator._

import oyun.user.{ UserContext }

final class PostApi(
  env: Env,
  maxPerPage: oyun.common.MaxPerPage,
  bus: oyun.common.Bus
) {

  import BSONHandlers._

  def makePost(
    categ: Categ,
    topic: Topic,
    data: DataForm.PostData)(implicit ctx: UserContext): Fu[Post] =
    lastNumberOf(topic) flatMap {
      case number =>
      val post = Post.make(
        topicId = topic.id,
        author = none,
        userId = ctx.me map (_.id),
        text = data.text,
        number = number + 1,
        categId = categ.id)
      env.postColl.insert(post) >>
      env.topicColl.update($id(topic.id), topic withPost post) >>
      env.categColl.update($id(categ.id), categ withTopic post) inject post
    }


  def urlData(postId: String): Fu[Option[PostUrlData]] = get(postId) flatMap {
    case Some((topic, post)) => PostRepo().countBeforeNumber(topic.id, post.number) map { nb =>
      val page = nb / maxPerPage.value + 1
      PostUrlData(topic.categId, topic.slug, page, post.number).some
    }
    case _ => fuccess(none)
  }

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

  def lastNumberOf(topic: Topic): Fu[Int] =
    PostRepo lastByTopic topic map { _ ?? (_.number) }
}
