package oyun.rating

final class Rating(var rating: Double, nbResults: Int) {

}


object RatingCalculator {

  def updateRating(rating: Rating, delta: Int) {
    rating.rating = rating.rating + delta
  }

  def updateMasaRatings(rating: Rating, rank: Int, hands: Int) {
    val oldRating = rating.rating

    val wager = hands * 4

    val ratingGain = rank match {
      case 1 => 0.5
      case 2 => 0.25
      case 3 => 0.15
      case _ => 0.1
    }
    val newRating = oldRating - (hands) + (wager * ratingGain)

    rating.rating = newRating
  }

}
