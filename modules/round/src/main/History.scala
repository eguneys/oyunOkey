package oyun.round

import oyun.game.Event

private[round] final class History(
  load: Fu[VersionedEvents]) {

  private var events: VersionedEvents = _

  def getVersion: Int = {
    waitForLoadedEvents
    events.headOption.??(_.version)
  }

  // none if version asked is > to history version
  // none if an event is missing (asked too old version)
  def getEventsSince(v: Int): Option[VersionedEvents] = {
    waitForLoadedEvents
    val version = getVersion
    if (v > version) none
    else if (v == version) Some(Nil)
    else events.takeWhile(_.version > v).reverse.some filter {
      case first :: rest => first.version == v + 1
      case _ => true
    }
  }

  def addEvents(xs: List[Event]): VersionedEvents = {
    waitForLoadedEvents
    val vevs = xs.foldLeft(List.empty[VersionedEvent] -> getVersion) {
      case ((vevs, v), e) => (VersionedEvent(e, v + 1) :: vevs, v + 1)
    }._1
    events = (vevs ::: events) take History.size
    vevs.reverse
  }

  private def waitForLoadedEvents {
    if (events == null) {
      events = load awaitSeconds 3
    }
  }
}

private[round] object History {

  val size = 30

  def apply()(gameId: String): History = new History(load = load(gameId))

  private def load(gameId: String): Fu[VersionedEvents] = fuccess(Nil)
}
