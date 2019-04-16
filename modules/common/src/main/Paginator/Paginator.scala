package oyun.common
package paginator

import scalaz.Success

final class Paginator[A] private[paginator] (
  val currentPage: Int,
  val maxPerPage: MaxPerPage,
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

  def previousPage: Option[Int] = (currentPage > 1) option (currentPage - 1)

  /**
    * Returns the next page.
    */
  def nextPage: Option[Int] = (currentPage < nbPages) option (currentPage + 1)

  /**
    * Returns the number of pages.
    */
  def nbPages: Int = scala.math.ceil(nbResults.toFloat / maxPerPage.value).toInt

  def hasToPaginate: Boolean = nbResults > maxPerPage.value

  def hasPreviousPage: Boolean = previousPage.isDefined

  def hasNextPage: Boolean = nextPage.isDefined
}

object Paginator {

  def apply[A](
    adapter: AdapterLike[A],
    currentPage: Int = 1,
    maxPerPage: MaxPerPage = MaxPerPage(10)): Fu[Paginator[A]] =
    validate(adapter, currentPage, maxPerPage) | apply(adapter, 1, maxPerPage)

  def validate[A](
    adapter: AdapterLike[A],
    currentPage: Int = 1,
    maxPerPage: MaxPerPage = MaxPerPage(10)): Valid[Fu[Paginator[A]]] =
    if (currentPage < 1) !!("Max per page must be greater than zero")
    else if (maxPerPage.value <= 0) !!("Current page must be greater than zero")
    else Success(for {
      results <- adapter.slice((currentPage - 1) * maxPerPage.value, maxPerPage.value)
      nbResults <- adapter.nbResults
    } yield new Paginator(currentPage, maxPerPage, results, nbResults))
}
