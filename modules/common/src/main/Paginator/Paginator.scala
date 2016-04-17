package oyun.common
package paginator

import scalaz.Success

final class Paginator[A] private[paginator] (
  val currentPage: Int,
  val maxPerPage: Int,
  /**
    * Returns the results for the current page.
    * The result is cached.
    */
  val currentPageResults: Seq[A],
  /**
    * Returns the number of results.
    * The result is cached.
    */
  val nbResults: Int) {

  /**
    * Returns the next page.
    */
  def nextPage: Option[Int] = (currentPage < nbPages) option (currentPage + 1)

  /**
    * Returns the number of pages.
    */
  def nbPages: Int = scala.math.ceil(nbResults.toFloat / maxPerPage).toInt
}

object Paginator {

  def apply[A](
    adapter: AdapterLike[A],
    currentPage: Int = 1,
    maxPerPage: Int = 10): Fu[Paginator[A]] =
    validate(adapter, currentPage, maxPerPage) | apply(adapter, 1, maxPerPage)

  def validate[A](
    adapter: AdapterLike[A],
    currentPage: Int = 1,
    maxPerPage: Int = 10): Valid[Fu[Paginator[A]]] =
    if (currentPage < 1) !!("Max per page must be greater than zero")
    else if (maxPerPage <= 0) !!("Current page must be greater than zero")
    else Success(for {
      results <- adapter.slice((currentPage - 1) * maxPerPage, maxPerPage)
      nbResults <- adapter.nbResults
    } yield new Paginator(currentPage, maxPerPage, results, nbResults))
}
