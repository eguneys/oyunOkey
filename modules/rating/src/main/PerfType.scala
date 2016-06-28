package oyun.rating

sealed abstract class PerfType(
  val id: Perf.ID,
  val key: Perf.Key,
  val name: String,
  val title: String,
  val iconChar: Char) {

  def iconString = iconChar.toString

}

object PerfType {

  case object Yuzbir extends PerfType(1,
    key = "yuzbir",
    name = "Yuzbir",
    title = "Yuzbir",
    iconChar = 'T')

  val all: List[PerfType] = List(Yuzbir)
  val byKey = all map { p => (p.key, p) } toMap
  val byId = all map { p => (p.id, p) } toMap

  val default = Yuzbir

  def apply(key: Perf.Key): Option[PerfType] = byKey get key
  def orDefault(key: Perf.Key): PerfType = apply(key) | default

  def apply(id: Perf.ID): Option[PerfType] = byId get id

  def id2key(id: Perf.ID): Option[Perf.Key] = byId get id map (_.key)

  val nonGame: List[PerfType] = List.empty

  def isGame(pt: PerfType) = !nonGame.contains(pt)

}
