package oyun.fishnet

import scala.util.{ Try, Success, Failure }

final class FishnetApi(
  repo: FishnetRepo,
  moveDb: MoveDB)(implicit system: akka.actor.ActorSystem) {

  def authenticateClient(): Fu[Try[Client]] = {
    repo.getOfflineClient map some
  } map {
    case None => Failure(new Exception("Can't authenticate: invalid key"))
    case Some(client) => Success(client)
  }

  def acquireMove(client: Client): Fu[Option[Work.Move]] =
    moveDb.acquire(client)

  def postMove(workId: Work.Id, client: Client, data: Option[okey.format.Uci]): Funit = fuccess {
    moveDb.postResult(workId, client, data)
  }

}
