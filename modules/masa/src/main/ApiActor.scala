package oyun.masa

import akka.actor._

import oyun.game.actorApi.FinishGame

private[masa] final class ApiActor(api: MasaApi) extends Actor {

  override def preStart {
  }

  def receive = {
    case FinishGame(game, _) => api finishGame game
  }

}
