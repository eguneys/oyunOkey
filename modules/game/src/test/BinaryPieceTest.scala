package oyun.game

import org.specs2.mutable._

import oyun.db.ByteArray

import okey.Piece

import Piece._

class BinaryPieceTest extends Specification {

  val noop = "00000000"
  def write(all: List[Piece]): List[String] =
    (BinaryFormat.piece write all).showBytes.split(',').toList
  def read(bytes: List[String]): List[Piece] =
    BinaryFormat.piece.read(ByteArray.parseBytes(bytes))

  "binary pieces" should {
    "write" should {
      "empty board" in {
        write(List.empty) must_== "" :: Nil
      }
      "Red 1" in {
        write(List(R1)) must_== "00000001" :: Nil
      }
      "Black 8" in {
        write(List(L8)) must_== "00011000" :: Nil
      }
      "Green 13" in {
        write(List(G13)) must_== "00101101" :: Nil
      }
      "Fake 1" in {
        write(List(F1)) must_== "01000001" :: Nil
      }
      "Blue 12" in {
        write(List(B12)) must_== "00111100" :: Nil
      }
      "Red 7 Black 10" in {
        write(List(R7, L10)) must_== "00000111" :: "00011010" :: Nil
      }
      "Blue 13 Green 9" in {
        write(List(B13, G9)) must_== "00111101" :: "00101001" :: Nil
      }
    }

    "read" should {
      "empty" in {
        read(Nil) must_== Nil
      }
      "Red 1" in {
        read("00000001" :: Nil) must_== List(R1)
      }
      "Fake 1" in {
        read("01000001" :: Nil) must_== List(F1)
      }
      "Black 13 Green 12 Blue 11 Red 10" in {
        read("00011101" :: "00101100" :: "00111011" :: "00001010" :: Nil) must_== List(L13, G12, B11, R10 )
      }
    }
  }

  // "binary pieces" should {
  //   "write" should {
  //     "empty board" in {
  //       write(List.empty) must_== "" :: Nil
  //     }
  //     "Red 1" in {
  //       write(List(R1)) must_== "00000100" :: Nil
  //     }
  //     "Black 8" in {
  //       write(List(L8)) must_== "01100000" :: Nil
  //     }
  //     "Green 13" in {
  //       write(List(G13)) must_== "10110100" :: Nil
  //     }
  //     "Blue 12" in {
  //       write(List(B12)) must_== "11110000" :: Nil
  //     }
  //     "Red 1 Black 10" in {
  //       write(List(R1, L10)) must_== "00000101" :: "10100000" :: Nil
  //     }
  //     "Blue 13 Green 9" in {
  //       write(List(B13, G9)) must_== "11110110" :: "10010000" :: Nil
  //     }
  //     "Green 12 Red 1 Black 10" in {
  //       write(List(G12, R1, L10)) must_== "10110000" :: "00010110" :: "10000000" :: Nil
  //     }
  //     "Black 2 Blue 13 Green 9" in {
  //       write(List(L2, B13, G9)) must_== "01001011" :: "11011010" :: "01000000" :: Nil
  //     }
  //     "Red 2 Black 13 Green 9 Blue 8" in {
  //       write(List(R2, L13, G9, B8)) must_== "00001001" :: "11011010" :: "01111000" :: Nil
  //     }
  //     "Blue 8, Black 13, Green 7, Red 12" in {
  //       write(List(B8, L13, G7, R12)) must_== "11100001" :: "11011001" :: "11001100" :: Nil
  //     }
  //   }

  //   "read" should {
  //     "empty" in {
  //       read(Nil) must_== Nil
  //     }
  //     "Red 1" in {
  //       read("00000100" :: Nil) must_== List(R1)
  //     }
  //   }
}
