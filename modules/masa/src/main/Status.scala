package oyun.masa

private[masa] sealed abstract class Status(val id: Int) extends Ordered[Status] {
  def compare(other: Status) = id compare other.id

  def name = toString
}

private[masa] object Status {
  case object Created extends Status(10)
  case object Started extends Status(20)
  case object Interrupted extends Status(25)
  case object Finished extends Status(30)

  val all = List(Created, Started, Interrupted, Finished)

  val byId = all map { v => (v.id, v) } toMap

  def apply(id: Int): Option[Status] = byId get id
}
