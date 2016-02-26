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

  def save(hook: Hook) {
    hooks = hooks.filterNot(_.id == hook.id) :+ hook
  }

  def remove(hook: Hook) {
    hooks = hooks filterNot (_.id == hook.id)
  }
}
