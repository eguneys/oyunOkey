package oyun.rating

final class Rating(var rating: Double, nbResults: Int) {

}


object RatingCalculator {

  def updateRating(rating: Rating, delta: Int) {
    rating.rating = rating.rating + delta
  }

}
