package oyun

package object game extends PackageObject with WithPlay {
  object tube {
    implicit lazy val gameTube = Game.tube inColl Env.current.gameColl
  }
}
