package oyun.game

private[game] case class Metadata(
  masaId: Option[String],
  roundAt: Int)

private[game] object Metadata {
  val empty = Metadata(None, 0)
}
