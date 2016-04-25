package oyun

import reactivemongo.api._
import reactivemongo.api.commands.WriteResult

package object db extends PackageObject with WithPlay {

  def recoverDuplicateKey[A](f: WriteResult => A): PartialFunction[Throwable, A] = {
    case e: WriteResult if e.code.contains(11000) => f(e)
  }

  private[db] def logger = oyun.log("db")
}
