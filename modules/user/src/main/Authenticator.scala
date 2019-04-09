package oyun.user

import reactivemongo.bson._

import oyun.db.dsl._

object Authenticator {


  implicit val HashedPasswordBsonHandler = new BSONHandler[BSONBinary, HashedPassword] {
    def read(b: BSONBinary) = HashedPassword(b.byteArray)
    def write(hash: HashedPassword) = BSONBinary(hash.bytes, Subtype.GenericBinarySubtype)
  }
}
