package oyun.fishnet

private final class FishnetRepo() {
  def getOfflineClient: Fu[Client] = fuccess(Client.offline)
}
