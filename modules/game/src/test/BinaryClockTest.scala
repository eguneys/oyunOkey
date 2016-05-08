package oyun.game

import scala.concurrent.duration._

import okey.Clock
import org.specs2.mutable._
import org.specs2.specification._

import oyun.db.ByteArray

class BinaryClockTest extends Specification {

  val _0_ = "00000000"
  val since = org.joda.time.DateTime.now.minusHours(1)
  def write(c: Clock): List[String] =
    (BinaryFormat.clock(since) write c).showBytes.split(',').toList
  def read(bytes: List[String]): Clock =
    BinaryFormat.clock(since).read(ByteArray.parseBytes(bytes))(okey.EastSide)

  def isomorphism(c: Clock): Clock =
    (BinaryFormat.clock(since).read(BinaryFormat.clock(since) write c)(okey.EastSide))

  "binary clock" should {
    val clock = Clock(120)
    val bits000 = List.fill(3)(_0_)
    val bits04 = List.fill(4)(_0_)

    "write" in {
      write(clock) must_== {
        List("01111000") ::: bits000 ::: bits000 ::: bits000 ::: bits000 ::: bits04
      }

      write(clock.addTime(okey.EastSide, 0.03f)) must_=={
        List("01111000") ::: List(_0_, _0_, "00000011") ::: bits000 ::: bits000 ::: bits000 ::: bits04
      }

      write(clock.addTime(okey.EastSide, -0.03f)) must_=={
        List("01111000") ::: List(_0_, _0_, "00000011") ::: bits000 ::: bits000 ::: bits000 ::: bits04
      }

      write(clock.addTime(okey.EastSide, -0.03f)) must_=={
        List("01111000") ::: List(_0_, _0_, "00000011") ::: bits000 ::: bits000 ::: bits000 ::: bits04
      }
    }

    "read" in {
      read(List("01111000") ::: bits000 ::: bits000 ::: bits000 ::: bits000 ::: bits04) must_== clock

      read(List("01111000") ::: List(_0_, _0_, "00000011") ::: bits000 ::: bits000 ::: bits000 ::: bits04) must_== clock.addTime(okey.EastSide, 0.03f)
    }


    "isomorphism" in {
      isomorphism(clock) must_== clock

      val c2 = clock.addTime(okey.EastSide, 15)
      isomorphism(c2) must_== c2

      val c3 = clock.addTime(okey.WestSide, 5)
      isomorphism(c3) must_== c3

      val c4 = clock.addTime(okey.NorthSide, 15)
      isomorphism(c4) must_== c4

      val c5 = clock.addTime(okey.SouthSide, 15)
      isomorphism(c5) must_== c5

      val c6 = clock.start
      isomorphism(c6).timerOption.get must beCloseTo(c6.timerOption.get, 1)

      Clock(120) |> { c =>
        isomorphism(c) must_== c
      }
    }
  }
}
