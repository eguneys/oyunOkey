package oyun.db

import org.joda.time.DateTime
import reactivemongo.bson._

trait Handlers {
  implicit object BSONJodaDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(x: BSONDateTime) = new DateTime(x.value)
    def write(x: DateTime) = BSONDateTime(x.getMillis)
  }
}
