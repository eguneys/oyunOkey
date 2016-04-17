package oyun.common
package paginator

trait AdapterLike[A] {

  /**
    * Returns the total number of results.
    */
  def nbResults: Fu[Int]

  /**
    * Returns a slice of the results.
    * 
    * @param  offset    The number of elements to skip, starting from zero
    * @param  length    The maximum number of elements to return
    */
  def slice(offset: Int, length: Int): Fu[Seq[A]]

}
