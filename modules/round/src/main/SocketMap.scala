package oyun.round

import scala.concurrent.duration._

import oyun.game.Game

private object SocketMap {

  def make(
    makeHistory: (String) => History,
    dependencies: RoundSocket.Dependencies,
    socketTimeout: FiniteDuration
  ): SocketMap = {

    import dependencies._

    val historyPersistenceEnabled = false

    lazy val socketMap: SocketMap = oyun.socket.SocketMap[RoundSocket](
      system = system,
      mkTrouper = (id: Game.ID) => new RoundSocket(
        dependencies = dependencies,
        gameId = id,
        history = makeHistory(id)
      ),
      accessTimeout = socketTimeout,
      monitoringName = "round.socketMap"
    )
    socketMap
  }

}
