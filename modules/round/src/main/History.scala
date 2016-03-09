package oyun.round

private[round] final class History(
  load: Fu[VersionedEvents]) {

  private var events: VersionedEvents = _

  def getVersion: Int = {
    waitForLoadedEvents
    events.headOption.??(_.version)
  }

  private def waitForLoadedEvents {
    if (events == null) {
      events = load awaitSeconds 3
    }
  }
}

private[round] object History {

  def apply()(gameId: String): History = new History(load = load(gameId))

  private def load(gameId: String): Fu[VersionedEvents] = fuccess(Nil)
}
