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
        write(OkeyPlayer(EastSide, None, true)) must_== "11111111" :: Nil
      }
      "some piece" in {
        write(OkeyPlayer(EastSide, Some(R1), false)) must_== "00000001" :: Nil
        write(OkeyPlayer(EastSide, Some(G13), false)) must_== "00101101" :: Nil
        write(OkeyPlayer(EastSide, Some(L8), true)) must_== "10011000" :: Nil
      }
    }

    "read" should {
      "empty piece" in {
        read("11111111" :: Nil) must_==  (None, true)
      }
      "some piece" in {
        read("00000001" :: Nil) must_== (Some(R1), false)
        read("00101101" :: Nil) must_== (Some(G13), false)
        read("10011000" :: Nil) must_== (Some(L8), true)
      }
    }
  }
}
