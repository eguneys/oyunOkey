package oyun.forum

import oyun.common.paginator._
import oyun.db.dsl._
import oyun.db.paginator._

import oyun.user.{ UserContext }

private[forum] final class TopicApi(
  env: Env,
  maxPerPage: oyun.common.MaxPerPage,
  bus: oyun.common.Bus
) {

  import BSONHandlers._

  def show(categSlug: String, slug: String, page: Int): Fu[Option[(Categ, Topic, Paginator[Post])]] =
    for {
      data <- (for {
        categ <- optionT(CategRepo bySlug categSlug)
        topic <- optionT(TopicRepo().byTree(categSlug, slug))
      } yield categ -> topic).run
      res <- data ?? {
        case (categ, topic) =>
          TopicRepo incViews topic
          env.postApi.paginator(topic, page) map { (categ, topic, _).some }
      }
    } yield res

  def makeTopic(
    categ: Categ,
    data: DataForm.TopicData
  )(implicit ctx: UserContext): Fu[Topic] =
    TopicRepo.nextSlug(categ, data.name) flatMap {
      case slug =>
        val topic = Topic.make(
          categId = categ.slug,
          slug = slug,
          name = data.name
        )
        val post = Post.make(
          topicId = topic.id,
          author = none,
          userId = ctx.me map (_.id),
          text = data.post.text,
          number = 1,
          categId = categ.id
        )
        env.postColl.insert(post) >>
        env.topicColl.insert(topic withPost post) >>
        env.categColl.update($id(categ.id), categ withTopic post) inject topic

    }

  def paginator(categ: Categ, page: Int): Fu[Paginator[TopicView]] = {
    val adapter = new Adapter[Topic](
      collection = env.topicColl,
      selector = TopicRepo() byCategQuery categ,
      projection = $empty,
      sort = $sort.updatedDesc
    ) mapFutureList { topics =>
      env.postColl.optionsByOrderedIds[Post, String](topics.map(_ lastPostId))(_.id) map { posts =>
        topics zip posts map {
          case topic ~ post => TopicView(categ, topic, post, env.postApi lastPageOf topic)
        }
      }
    }

    val cachedAdapter = new CachedAdapter(adapter, nbResults = fuccess(1000))
    Paginator(
      adapter = cachedAdapter,
      currentPage = page,
      maxPerPage = maxPerPage
    )
  }

}
