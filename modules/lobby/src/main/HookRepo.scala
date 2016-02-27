package oyun.lobby

object HookRepo {

  private var hooks = Vector[Hook]()

  private val hardLimit = 100

  def list = {
    if (hooks.size > hardLimit) {
      // 
    }
    hooks.toList
  }

  def byUid(uid: String) = hooks find (_.uid == uid)

  def notInUids(uids: Set[String]): List[Hook] = list.filterNot(h => uids(h.uid))

  def save(hook: Hook) {
    hooks = hooks.filterNot(_.id == hook.id) :+ hook
  }

  def remove(hook: Hook) {
    hooks = hooks filterNot (_.id == hook.id)
  }
}
