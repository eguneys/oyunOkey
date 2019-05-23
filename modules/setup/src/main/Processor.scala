package oyun.setup

import akka.actor.ActorSelection
import akka.pattern.ask

import oyun.lobby.actorApi.{ AddHook }
import oyun.user.{ UserContext }
import oyun.masa.{ Masa, PlayerRef }

private[setup] final class Processor(
  bus: oyun.common.Bus,
  masaApi: oyun.masa.MasaApi
) {

  def ai(config: AiConfig, playerRef: PlayerRef)(implicit ctx: UserContext): Fu[Masa] = {
    val masaSetup = config.masa()
    masaApi.addMasa(masaSetup, playerRef) addEffect { masa =>
      // bus.publish(AddHook(masa), 'lobbyTrouper)
      masaApi.invite(masa.id) >>
      masaApi.invite(masa.id) >>
      masaApi.invite(masa.id)
    }
  }

  def masa(
    config: MasaConfig,
    playerRef: PlayerRef,
    uid: oyun.socket.Socket.Uid,
    sid: Option[String])(implicit ctx: UserContext): Fu[Masa] = {

    val masaSetup = config.masa()
    masaApi.addMasa(masaSetup, playerRef) addEffect { masa =>
      // bus.publish(AddHook(masa), 'lobbyTrouper)
    }
  }
}
