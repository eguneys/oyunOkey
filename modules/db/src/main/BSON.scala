package oyun.db

import org.joda.time.DateTime
import reactivemongo.bson._

abstract class BSON[T]
    extends BSONHandler[BSONDocument, T]
    with BSONDocumentReader[T]
    with BSONDocumentWriter[T] {

  import BSON._

  def reads(reader: Reader): T
  def writes(writer: Writer, obj: T): BSONDocument

  def read(doc: BSONDocument): T = reads(new Reader(doc))
  def write(obj: T): BSONDocument = writes(writer, obj)
}

object BSON {

  implicit object BSONJodaDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(x: BSONDateTime) = new DateTime(x.value)
    def write(x: DateTime) = BSONDateTime(x.getMillis)
  }

  final class Reader(val doc: BSONDocument) {
    val map = {
      val b = collection.immutable.Map.newBuilder[String, BSONValue]
      for (tuple <- doc.stream if tuple.isSuccess) b += (tuple.get._1 -> tuple.get._2)
      b.result
    }

    def get[A](k: String)(implicit reader: BSONReader[_ <: BSONValue, A]): A =
      reader.asInstanceOf[BSONReader[BSONValue, A]] read map(k)

    def getO[A](k: String)(implicit reader: BSONReader[_ <: BSONValue, A]) =
      map get k flatMap reader.asInstanceOf[BSONReader[BSONValue, A]].readOpt

    def str(k: String) = get[String](k)
    def strO(k: String) = getO[String](k)
    def int(k: String) = get[Int](k)
    def bool(k: String) = get[Boolean](k)
    def bytes(k: String) = get[ByteArray](k)
    def bytesO(k: String) = getO[ByteArray](k)
  }

  final class Writer {
  }

  val writer = new Writer

  def debug(v: BSONValue): String = v match {
    case d: BSONDocument => debugDoc(d)
    case d: BSONArray => debugArr(d)
    case v => v.toString
  }

  def debugArr(doc: BSONArray): String = doc.values.toList.map(debug).mkString("[", ", ", "]")
  def debugDoc(doc: BSONDocument): String = (doc.elements.toList map {
    case (k, v) => s"$k: ${debug(v)}"
  }).mkString("{", ", ", "}")


  def asStrings(vs: List[BSONValue]): List[String] = {
    val b = new scala.collection.mutable.ListBuffer[String]
    vs foreach {
      case BSONString(s) => b += s
      case _ =>
    }
    b.toList
  }
}
