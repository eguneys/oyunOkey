package controllers

import scala.concurrent.duration._

import io.prismic.Fragment.DocumentLink
import io.prismic.{ Api => PrismicApi, _ }

import oyun.app._
import oyun.memo.AsyncCache

object Prismic {

  private val logger = oyun.log("prismic")

  val prismicLogger = (level: Symbol, message: String) => level match {
    case a => println(a); throw new Exception
    case 'DEBUG => logger debug message
    case 'ERROR => logger error message
    case _ => logger info message
  }

  private val fetchPrismicApi = AsyncCache.single[PrismicApi](
    f = PrismicApi.get(Env.api.PrismicApiUrl, logger = prismicLogger),
    timeToLive = 1 minute)

  def prismicApi = fetchPrismicApi(true)

  implicit def makeLinkResolver(prismicApi: PrismicApi, ref: Option[String] = None) =
    DocumentLinkResolver(prismicApi) {
      //case (DocumentLink(id, _, _, slug, false), _) => routes.
      case _ => routes.Lobby.home.url
    }

  def getVariant(variant: okey.variant.Variant) = prismicApi flatMap { api =>
    api.forms("variant")
      .query(s"""[[:d = at(my.variant.key, "${variant.key}")]]""")
      .ref(api.master.ref)
      .submit() map {
      _.results.headOption map (_ -> makeLinkResolver(api))
    }
  }
}
