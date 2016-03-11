package oyun.masa

import okey.Side

private[masa] final class MasaApi() {

  def createMasa(setup: MasaSetup, player: PlayerRef): Fu[Masa] = {
    val masa = Masa.make()

    MasaRepo.insert(masa) >>- join(masa.id, player) inject masa
  }


  def join(masaId: String, player: PlayerRef, side: Side = Side.EastSide) {
    Sequencing(masaId)(MasaRepo.enterableById) { masa =>
      PlayerRepo.join(masa.id, player, side) >>- {

      }
    }
  }


  private def sequence(masaId: String)(work: => Funit) {
    //sequencers ! Tell(masaId, Sequencer work work)
    (() => work)()
  }

  private def Sequencing(masaId: String)(fetch: String => Fu[Option[Masa]])(run: Masa => Funit) {
    sequence(masaId) {
      fetch(masaId) flatMap {
        case Some(m) => run(m)
        case None => fufail(s"Can't run sequence opeartion on missing masa $masaId")
      }
    }
  }
}
