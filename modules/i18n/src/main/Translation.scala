package oyun.i18n

import play.twirl.api.Html
import scalatags.Text.RawFrag

private sealed trait Translation

private final class Simple(val message: String) extends Translation {

  def formatTxt(args: Seq[Any]): String =
    if (args.isEmpty) message
    else message.format(args: _*)

  def formatFrag(args: Seq[RawFrag]): RawFrag =
    if (args.isEmpty) RawFrag(message)
    else RawFrag(message.format(args.map(_.v): _*))


  def formatHtml(args: Seq[Html]): Html =
    if (args.isEmpty) Html(message)
    else Html(message.format(args.map(_.body): _*))
}
