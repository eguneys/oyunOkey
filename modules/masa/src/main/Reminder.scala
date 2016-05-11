package oyun.masa

import akka.actor._
import akka.pattern.{ ask, pipe }
import play.api.libs.json.Json
import play.twirl.api.Html

import actorApi._
import oyun.hub.actorApi.SendTos
import makeTimeout.short

private[masa] final class Reminder(
  renderer: ActorSelection) extends Actor {

  private val bus = context.system.oyunBus

  private val max = 100

  def receive = {
    case msg@RemindMasa(masa, activeUserIds) =>
      renderer ? msg foreach {
        case html: Html =>
          val userIds =
            if (activeUserIds.size > max) scala.util.Random.shuffle(activeUserIds) take max
            else activeUserIds
          bus.publish(SendTos(userIds.toSet, Json.obj(
            "t" -> "masaReminder",
            "d" -> Json.obj(
              "id" -> masa.id,
              "html" -> html.toString
            ))), 'users)
      }
  }
}
