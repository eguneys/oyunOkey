package oyun.common

object Form {
  def errorsAsJson(form: play.api.data.Form[_])(implicit lang: play.api.i18n.Messages) =
    form.errorsAsJson
}
