package oyun

package object game extends PackageObject with WithPlay {
  private[game] def logger = oyun.log("game")
}
