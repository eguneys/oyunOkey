package oyun.i18n

import oyun.common.Lang

private sealed trait I18nQuantity

private object I18nQuantity {

  case object Zero extends I18nQuantity
  case object One extends I18nQuantity
  case object Other extends I18nQuantity

  type Language = String
  type Selector = Count => I18nQuantity

  def apply(lang: Lang, c: Count): I18nQuantity =
    langMap.getOrElse(lang.language, selectors.default _)(c)

  private object selectors {
    def default(c: Count) =
      if (c == 1) One
      else Other
  }

  import selectors._
  private val langMap: Map[Language, Selector] = LangList.all.map {
    case (lang, _) => lang.language -> (lang.language match {
      case _ => default _
    })
  }
}
