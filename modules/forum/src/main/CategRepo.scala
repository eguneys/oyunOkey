package oyun.forum

import oyun.db.dsl._

object CategRepo {

  import BSONHandlers.CategBSONHandler

  private val coll = Env.current.categColl

  def bySlug(slug: String) = coll.byId[Categ](slug)

  def list(): Fu[List[Categ]] =
    coll.find($empty).sort($sort asc "pos").cursor[Categ]().gather[List]()

}
