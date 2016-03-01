package oyun.db

import reactivemongo.bson._

case class ByteArray(value: Array[Byte]) {

  def isEmpty = value.isEmpty

  def showBytes: String = value map { b =>
    "%08d" format { b & 0xff }.toBinaryString.toInt
  } mkString ","
}


object ByteArray {

  import ornicar.scalalib.Zero

  implicit def ByteArrayZero: Zero[ByteArray] =
    Zero.instance(ByteArray(Array.empty))

  implicit object ByteArrayBSONHandler extends BSONHandler[BSONBinary, ByteArray] {
    def read(bin: BSONBinary) = ByteArray(bin.byteArray)

    def write(ba: ByteArray) = BSONBinary(ba.value, subtype)
  }

  def parseByte(s: String): Byte = {
    val l = s grouped 2 map {
      case "00" => 0
      case "01" => 1
      case "10" => 2
      case "11" => 3
      case x => sys error s"invalid binary literal: $x in $s"
    } toList

    return (l(0) << 6) + (l(1) << 4) + (l(2) << 2) + l(3) toByte
  }

  def parseBytes(s: List[String]) = ByteArray(s map parseByte toArray)

  def subtype = Subtype.GenericBinarySubtype
}
