package oyun.lobby

import oyun.socket.Socket.Uid

object HookRepo {

  private var hooks = Vector[Hook]()

  private val hardLimit = 100

  def list = {
    if (hooks.size > hardLimit) {
      // 
    }
    hooks.toList
  }

  def byId(id: String) = hooks find (_.id == id)

  def byUid(uid: Uid) = hooks find (_.uid == uid)

  def notInUids(uids: Set[Uid]): List[Hook] = list.filterNot(h => uids(h.uid))

  def save(hook: Hook) {
    hooks = hooks.filterNot(_.id == hook.id) :+ hook
  }

  def remove(hook: Hook) {
    hooks = hooks filterNot (_.id == hook.id)
  }

  def update(hook: Hook) {
    save(hook)
  }
}
