package oyun.masa

private[masa] final class MasaApi() {

  def createMasa(setup: MasaSetup): Fu[Masa] = {
    val masa = Masa.make()

    MasaRepo.insert(masa) inject masa
  }
}
