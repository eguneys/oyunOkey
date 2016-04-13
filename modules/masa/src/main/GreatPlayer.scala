package oyun.masa

object GreatPlayer {
  val all: Map[String, String] = Map(
    "Manço" -> "Barış_Manço"
  )

  private val size = all.size
  private val names = all.keys.toVector

  def randomName: String = names(scala.util.Random nextInt size)

  def wikiUrl(name: String) = all get name map { s =>
    s"https://tr.wikipedia.org/wiki/$s"
  }
}
