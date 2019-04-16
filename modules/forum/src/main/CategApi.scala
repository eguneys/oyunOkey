package oyun.forum

import oyun.common.paginator._
import oyun.db.dsl._

private[forum] final class CategApi(env: Env) {

  def list(): Fu[List[CategView]] = for {
    categs <- CategRepo.list()
    views <- (categs map { categ =>
      env.postApi get (categ lastPostId) map { topicPost =>
        CategView(categ, topicPost map {
          _ match {
            case (topic, post) => (topic, post, env.postApi lastPageOf topic)
          }
        })
      }
    }).sequenceFu
  } yield views

  def show(slug: String, page: Int): Fu[Option[(Categ, Paginator[TopicView])]] =
    optionT(CategRepo bySlug slug) flatMap { categ =>
      optionT(env.topicApi.paginator(categ, page) map { (categ, _).some })
    } run

}
