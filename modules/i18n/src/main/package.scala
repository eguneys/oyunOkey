package oyun

package object i18n extends PackageObject with WithPlay {
  type Messages = Map[String, Map[String, String]]

  private[i18n] def logger = oyun.log("i18n")
}
