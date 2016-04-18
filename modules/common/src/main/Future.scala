package oyun.common

object Future {
  def traverseSequentially[A, B](list: List[A])(f: A => Fu[B]): Fu[List[B]] =
    list match {
      case h :: t => f(h).flatMap { r =>
        traverseSequentially(t)(f) map (r +: _)
      }
      case Nil => fuccess(Nil)
    }
}
