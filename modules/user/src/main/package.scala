package oyun

package object user extends PackageObject with WithPlay {
  private[user] def logger = oyun.log("user")
}
