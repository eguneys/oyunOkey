package oyun.masa

import reactivemongo.bson._

import oyun.db.BSON

object BSONHandlers {

  implicit val masaHandler = new BSON[Masa] {
    def reads(r: BSON.Reader) = {
      Masa(
        id = r str "_id"
      )
    }

    def writes(w: BSON.Writer, o: Masa) = BSONDocument(
      "_id" -> o.id
    )
  }
}
