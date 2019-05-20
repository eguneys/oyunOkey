package oyun.common

case class MaxPerPage(value: Int) extends AnyVal with IntValue

case class AssetVersion(value: String) extends AnyVal with StringValue

object AssetVersion {

  var current = random
  def change = { current = random }
  private def random = AssetVersion(ornicar.scalalib.Random secureString 6)

}
