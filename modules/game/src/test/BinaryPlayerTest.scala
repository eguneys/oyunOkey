package oyun.game

import org.specs2.mutable._

import oyun.db.ByteArray

import okey.{ Side, Piece, Player => OkeyPlayer }

import okey. { EastSide }
import Piece._

class BinaryPlayerTest extends Specification {
  "binary player" should {
    def write(all: OkeyPlayer): List[String] =
      (BinaryFormat.player write all).showBytes.split(',').toList
    def read(bytes: List[String]): (Option[Piece], Boolean) =
      BinaryFormat.player.read(ByteArray.parseBytes(bytes))

    "write" should {
      "empty piece" in {
        write(OkeyPlayer(EastSide, drawMiddle = true, drawLeft = None)) must_== "11111111" :: Nil
        write(OkeyPlayer(EastSide, drawMiddle = false, None)) must_== "01111111" :: Nil
      }
      "some piece" in {
        write(OkeyPlayer(EastSide, drawMiddle = false, Some(R1))) must_== "00000001" :: Nil
        write(OkeyPlayer(EastSide, drawMiddle = false, Some(G13))) must_== "00101101" :: Nil
        write(OkeyPlayer(EastSide, drawMiddle = true, Some(L8))) must_== "10011000" :: Nil
      }
    }

    "read" should {
      "empty piece" in {
        read("11111111" :: Nil) must_==  (None, true)
        read("01111111" :: Nil) must_==  (None, false)
      }
      "some piece" in {
        read("00000001" :: Nil) must_== (Some(R1), false)
        read("00101101" :: Nil) must_== (Some(G13), false)
        read("10011000" :: Nil) must_== (Some(L8), true)
      }
    }
  }
}
