package oyun.round

import oyun.game.{ GameRepo, Game, PerfPicker }
import oyun.user.{ UserRepo, User, Perfs }

import oyun.rating.{ GliOkey, Perf, PerfType => PT, Rating, RatingCalculator }

import okey.{ Sides }

final class PerfsUpdater() {

  def save(game: Game, users: okey.Sides[User]): Funit =
    PerfPicker.main(game) ?? { mainPerf =>
      (game.rated && game.finished && game.accountable) ?? {
        val ratings = users map { u => mkRatings(u.perfs) }
        val result = resultOf(game)
        game.ratingVariant match {
          case okey.variant.StandardTest =>
            updateRatings(ratings map(_.yuzbir), result map GliOkey.Result.apply)
          case okey.variant.Standard =>
            updateRatings(ratings map(_.yuzbir), result map GliOkey.Result.apply)
          case _ => ratings
        }

        val perfs = users zip ratings map { case (user, ratings) => mkPerfs(ratings, user.perfs, game) }
        def intRatingLens(perfs: Perfs) = mainPerf(perfs).gliokey.intRating

        {
          (users zip perfs).map { case (user, perfs) =>
            UserRepo.setPerfs(user, perfs, user.perfs)
          }.sequenceFu
        }
      }.void
    }

  private final case class Ratings(
    yuzbir: Rating)

  private def resultOf(game: Game): Sides[Int] = game.players map {
    _.userId.flatMap(game.endStandingByUser(_)) getOrElse 4
  }

  private def mkRatings(perfs: Perfs) = new Ratings(
    yuzbir = perfs.yuzbir.toRating)
  

  private def updateRatings(ratings: Sides[Rating], results: Sides[GliOkey.Result]) {
    ratings zip results map { case (rating, result) =>
      val delta = result match {
        case GliOkey.Result.First => 2
        case GliOkey.Result.Second => 1
        case GliOkey.Result.Third => -1
        case GliOkey.Result.Fourth => -2
      }

      RatingCalculator.updateRating(rating, delta)
    }
  }

  private def mkPerfs(ratings: Ratings, perfs: Perfs, game: Game): Perfs = {
    val date = game.updatedAt | game.createdAt

    def addRatingIf(cond: Boolean, perf: Perf, rating: Rating) =
      if (cond) perf.addOrReset(_.round.error.gliokey, s"game ${game.id}")(rating, date)
      else perf


    val perfs1 = perfs.copy(
      yuzbir = addRatingIf(game.ratingVariant.yuzbir, perfs.yuzbir, ratings.yuzbir))

    val r = oyun.rating.Regulator
    val perfs2 = perfs1.copy(
      yuzbir = r(PT.Yuzbir, perfs.yuzbir, perfs1.yuzbir))

    perfs2
  }
}
