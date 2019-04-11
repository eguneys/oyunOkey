package oyun.masa

import akka.actor._

import oyun.game.actorApi.{ FinishGame, WithdrawMasa }

private[masa] final class ApiActor(api: MasaApi) extends Actor {

  override def preStart {
  }

  def receive = {
    case FinishGame(game, _) => api finishGame game

    case WithdrawMasa(masaId, playerId) => api.withdraw(masaId, playerId)
  }

}
