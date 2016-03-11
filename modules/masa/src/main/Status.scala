package oyun.masa

private[masa] sealed abstract class Status(val id: Int) extends Ordered[Status] {
  def compare(other: Status) = id compare other.id

  def name = toString
}

private[masa] object Status {
  case object Created extends Status(10)
}
