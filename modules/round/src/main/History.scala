package oyun.round

import oyun.game.Event
import oyun.db.dsl._

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
    val date = nowSeconds
    val vevs = xs.foldLeft(List.empty[VersionedEvent] -> getVersion) {
      case ((vevs, v), e) => (VersionedEvent(e, v + 1, date) :: vevs, v + 1)
    }._1
    events = (vevs ::: events) take History.size
    vevs.reverse
  }

  private def waitForLoadedEvents {
    if (events == null) {
      events = load awaitSeconds 3
    }
  }


  /* if v+1 refers to an old event,
   * then the client probably has skipped events somehow.
   * Log and send new events.
   * None => client is too late, or has greater version than server. Resync.
   * Some(List.empty) => all is good, do nothing
   * Some(List.nonEmpty) => late client, send new events
   * 
   * We check the event age because if the client sends a
   * versionCheck ping while the server sends an event,
   * we can get a false positive
   * 
   */
  def versionCheck(v: Int): Option[List[VersionedEvent]] =
    getEventsSince(v) map { evs =>
      if (evs.headOption.exists(_ hasSeconds 7)) evs else Nil
    }
}

private[round] object History {

  val size = 30

  def apply(coll: Coll)(gameId: String): History = new History(
    load = load(coll, gameId)
  )

  private def load(coll: Coll, gameId: String): Fu[VersionedEvents] =
    coll.byId[Bdoc](gameId).map {
      _.flatMap(_.getAs[VersionedEvents]("e")) ?? (_.reverse)
    } addEffect {
      case events if events.nonEmpty => coll.remove($id(gameId)).void
      case _ =>
    }

  def apply()(gameId: String): History = new History(load = load(gameId))

  private def load(gameId: String): Fu[VersionedEvents] = fuccess(Nil)

}
