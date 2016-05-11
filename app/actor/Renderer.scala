package oyun.app
package actor

import akka.actor._
import play.twirl.api.Html

import views.{ html => V }

private[app] final class Renderer extends Actor {

  def receive = {
    case oyun.masa.actorApi.RemindMasa(masa, _) =>
      sender ! spaceless(V.masa.reminder(masa))

    case oyun.masa.actorApi.MasaTable(masas) =>
      sender ! spaceless(V.masa.enterable(masas))
  }


  private val spaceRegex = """\s{2,}""".r
  private def spaceless(html: Html) = Html {
    spaceRegex.replaceAllIn(html.body.replace("\\n", " "), " ")
  }
}
