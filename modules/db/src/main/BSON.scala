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

  object MapDocument {
    implicit def MapReader[V](implicit vr: BSONDocumentReader[V]): BSONDocumentReader[Map[String, V]] = new BSONDocumentReader[Map[String, V]] {
      def read(bson: BSONDocument): Map[String, V] = {
        // mutable optimized implementation
        val b = collection.immutable.Map.newBuilder[String, V]
        for (tuple <- bson.elements)
          // assume that all values in the document are BSONDocuments
          b += (tuple._1 -> vr.read(tuple._2.asInstanceOf[BSONDocument]))
        b.result
      }
    }

    implicit def MapWriter[V](implicit vw: BSONDocumentWriter[V]): BSONDocumentWriter[Map[String, V]] = new BSONDocumentWriter[Map[String, V]] {
      def write(map: Map[String, V]): BSONDocument = BSONDocument {
        map.toStream.map { tuple =>
          tuple._1 -> vw.write(tuple._2)
        }
      }
    }

    implicit def MapHandler[V](implicit vr: BSONDocumentReader[V], vw: BSONDocumentWriter[V]): BSONHandler[BSONDocument, Map[String, V]] = new BSONHandler[BSONDocument, Map[String, V]] {
      private val reader = MapReader[V]
      private val writer = MapWriter[V]
      def read(bson: BSONDocument): Map[String, V] = reader read bson
      def write(map: Map[String, V]): BSONDocument = writer write map
    }
  }

  object MapValue {
    implicit def MapReader[V](implicit vr: BSONReader[_ <: BSONValue, V]): BSONDocumentReader[Map[String, V]] = new BSONDocumentReader[Map[String, V]] {
      def read(bson: BSONDocument): Map[String, V] = {
        val valueReader = vr.asInstanceOf[BSONReader[BSONValue, V]]
        // mutable optimized implementation
        val b = collection.immutable.Map.newBuilder[String, V]
        for (tuple <- bson.elements) b += (tuple._1 -> valueReader.read(tuple._2))
        b.result
      }
    }

    implicit def MapWriter[V](implicit vw: BSONWriter[V, _ <: BSONValue]): BSONDocumentWriter[Map[String, V]] = new BSONDocumentWriter[Map[String, V]] {
      def write(map: Map[String, V]): BSONDocument = BSONDocument {
        map.toStream.map { tuple =>
          tuple._1 -> vw.write(tuple._2)
        }
      }
    }

    implicit def MapHandler[V](implicit vr: BSONReader[_ <: BSONValue, V], vw: BSONWriter[V, _ <: BSONValue]): BSONHandler[BSONDocument, Map[String, V]] = new BSONHandler[BSONDocument, Map[String, V]] {
      private val reader = MapReader[V]
      private val writer = MapWriter[V]
      def read(bson: BSONDocument): Map[String, V] = reader read bson
      def write(map: Map[String, V]): BSONDocument = writer write map
    }
  }

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
    def int(i: Int): BSONInteger = BSONInteger(i)
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
