package oyun.masa

import oyun.game.PerfPicker

import oyun.user.{ UserRepo, User, Perfs }

import oyun.rating.{ GliOkey, Perf, PerfType => PT, Rating, RatingCalculator }

import okey.{ Sides }

final class PerfsUpdater() {

  def save(masa: Masa, userPlayers: okey.Sides[(User, RankedPlayer)]): Funit =
    PerfPicker.main(masa.variant) ?? { mainPerf =>
      (masa.rated) ?? {
        val users = userPlayers.map(_._1)
        val result = userPlayers.map(_._2.rank)
        val ratings = users map { u => mkRatings(u.perfs) }
        masa.variant match {
          case okey.variant.StandardTest =>
            masa.rounds map { updateRatings(_, ratings map(_.yuzbir), result map GliOkey.Result.apply) }
          case okey.variant.Standard =>
            masa.rounds map { updateRatings(_, ratings map(_.yuzbir), result map GliOkey.Result.apply) }
          case _ => ratings
        }

        val perfs = users zip ratings map { case (user, ratings) => mkPerfs(ratings, user.perfs, masa) }
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

  private def mkRatings(perfs: Perfs) = new Ratings(
    yuzbir = perfs.yuzbir.toRating)
  

  private def updateRatings(hands: Int, ratings: Sides[Rating], results: Sides[GliOkey.Result]) {
    ratings zip results map { case (rating, result) =>
      RatingCalculator.updateMasaRatings(rating, result.v, hands)
    }
  }

  private def mkPerfs(ratings: Ratings, perfs: Perfs, masa: Masa): Perfs = {
    val date = masa.createdAt

    def addRatingIf(cond: Boolean, perf: Perf, rating: Rating) =
      if (cond) perf.addOrReset(_.round.error.gliokey, s"masa ${masa.id}")(rating, date)
      else perf


    val perfs1 = perfs.copy(
      yuzbir = addRatingIf(masa.variant.yuzbir, perfs.yuzbir, ratings.yuzbir))

    val r = oyun.rating.Regulator
    val perfs2 = perfs1.copy(
      yuzbir = r(PT.Yuzbir, perfs.yuzbir, perfs1.yuzbir))

    perfs2
  }
}
