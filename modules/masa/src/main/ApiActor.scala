package oyun.masa

import akka.actor._

import oyun.game.actorApi.FinishGame

private[masa] final class ApiActor(api: MasaApi) extends Actor {

  override def preStart {
    context.system.oyunBus.subscribe(self, 'finishGame)
  }

  def receive = {
    case FinishGame(game) => api finishGame game
  }

}
