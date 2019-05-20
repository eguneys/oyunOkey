package oyun.i18n

import oyun.common.Lang

object I18nDb {

  sealed trait Ref
  case object Site extends Ref
  case object Arena extends Ref

  val site: Messages = oyun.i18n.db.site.Registry.load
  val arena: Messages = oyun.i18n.db.arena.Registry.load


  def apply(ref: Ref): Messages = ref match {
    case Site => site
    case Arena => arena
  }

  val langs: Set[Lang] = site.keys.map(Lang.apply)(scala.collection.breakOut)

}
