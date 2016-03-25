package oyun.i18n

import play.api.i18n.Lang

case class I18nDomain(domain: String) {

  lazy val parts = domain.split('.').toList

  lazy val lang: Option[Lang] =
    parts.headOption filter (_.size == 2) map { Lang(_, "") }
}
