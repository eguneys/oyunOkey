package oyun.i18n

// import play.api.i18n.Lang
// import play.api.mvc.{ RequestHeader }

// private[i18n] case class I18nPool(val langs: Set[Lang], val default: Lang) {

//   private val cache = scala.collection.mutable.Map[String, Option[Lang]]()

//   val names: Map[String, String] = (langs map langNames).toMap

//   private def langNames(lang: Lang): (String, String) =
//     lang.language -> LangList.nameOrCode(lang.language)

//   def lang(req: RequestHeader) = domainLang(req) getOrElse default

//   def preferred(req: RequestHeader) =
//     (req.acceptLanguages find langs.contains) getOrElse default

//   def domainLang(req: RequestHeader) =
//     cache.getOrElseUpdate(req.domain, {
//       I18nDomain(req.domain).lang filter langs.contains
//     })

// }
