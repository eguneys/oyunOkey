package oyun.db

import org.joda.time.DateTime
import reactivemongo.bson._

import oyun.common.Iso._
import oyun.common.{ Iso }

trait Handlers {
  implicit object BSONJodaDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(x: BSONDateTime) = new DateTime(x.value)
    def write(x: DateTime) = BSONDateTime(x.getMillis)
  }



  def isoHandler[A, B, C <: BSONValue](iso: Iso[B, A])(implicit handler: BSONHandler[C, B]): BSONHandler[C, A] = new BSONHandler[C, A] {
    def read(x: C): A = iso.from(handler read x)
    def write(x: A): C = handler write iso.to(x)
  }
  def isoHandler[A, B, C <: BSONValue](to: A => B, from: B => A)(implicit handler: BSONHandler[C, B]): BSONHandler[C, A] =
    isoHandler(Iso(from, to))


def stringIsoHandler[A](implicit iso: StringIso[A]): BSONHandler[BSONString, A] = isoHandler[A, String, BSONString](iso)
  def stringAnyValHandler[A](to: A => String, from: String => A): BSONHandler[BSONString, A] = stringIsoHandler(Iso(from, to))

  implicit def bsonArrayToListHandler[T](implicit reader: BSONReader[_ <: BSONValue, T], writer: BSONWriter[T, _ <: BSONValue]): BSONHandler[BSONArray, List[T]] = new BSONHandler[BSONArray, List[T]] {
    def read(array: BSONArray) = readStream(array, reader.asInstanceOf[BSONReader[BSONValue, T]]).toList
    def write(repr: List[T]) =
      new BSONArray(repr.map(s => scala.util.Try(writer.write(s))).to[Stream])
  }

  private def readStream[T](array: BSONArray, reader: BSONReader[BSONValue, T]): Stream[T] = {
    array.stream.filter(_.isSuccess).map { v =>
      reader.read(v.get)
    }
  }
}
