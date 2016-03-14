package oyun.game

private[game] case class Metadata(
  masaId: Option[String])

private[game] object Metadata {
  val empty = Metadata(None)
}
