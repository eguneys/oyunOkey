package oyun

package object db extends PackageObject with WithPlay {
  type TubeInColl[A] = Tube[A] with InColl[A]
  type BsTubeInColl[A] = BsTube[A] with InColl[A]

  private[db] def logger = oyun.log("db")
}
