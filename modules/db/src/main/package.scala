package oyun

package object db extends PackageObject with WithPlay {
  private[db] def logger = oyun.log("db")
}
