package oyun

import oyun.common.Lang

package object i18n extends PackageObject with WithPlay {

  type Count = Int
  type MessageKey = String

  private[i18n] type MessageMap = java.util.Map[MessageKey, Translation]
  private[i18n] type Messages = Map[play.api.i18n.Lang, MessageMap]

  private[i18n] def logger = oyun.log("i18n")

  val trLang = Lang("tr", "TR")
  val defaultLang = trLang
}
