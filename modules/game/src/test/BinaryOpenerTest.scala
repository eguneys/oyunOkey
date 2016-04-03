package oyun.game

import org.specs2.mutable._

import oyun.db.ByteArray

import okey._

import Piece._

class BinaryOpenerTest extends Specification {

  val noop = "00000000"

  "binary opener" should {
    "series" should {
      def write(all: List[(Side, OpenSerie)]): List[String] =
        (BinaryFormat.opener writeSeries all).showBytes.split(',').toList
      def read(bytes: List[String]): List[(Side, OpenSerie)] =
        BinaryFormat.opener.readSeries(ByteArray.parseBytes(bytes))
      "write" should {
        "empty opens" in {
          write(List.empty) must_== "" :: Nil
        }
        "empty pieces" in {
          write(List(EastSide -> OpenSerie(Nil, 0))) must_== "00000000" :: "00000000" :: Nil
          write(List(WestSide -> OpenSerie(Nil, 0))) must_== "00010000" :: "00000000":: Nil
          write(List(NorthSide -> OpenSerie(Nil, 0))) must_== "00100000" :: "00000000"::  Nil
          write(List(SouthSide -> OpenSerie(Nil, 0))) must_== "00110000" :: "00000000":: Nil
        }
        "more pieces" in {
          write(List(
            EastSide -> OpenSerie(List(R1, R2), 3),
            EastSide -> OpenSerie(List(L1, L2, L3, L4), 10),
            WestSide -> OpenSerie(List(R12), 12),
            SouthSide -> OpenSerie(List(G3), 3),
            NorthSide -> OpenSerie(List(R7, R8, R1, R2, R3), 21))) must_==
          "00000010" :: "00000011" :: "00000001" :: "00000010" ::
          "00000100" :: "00001010" :: "00010001" :: "00010010" :: "00010011" :: "00010100" ::
          "00010001" :: "00001100" :: "00001100" ::
          "00110001" :: "00000011" :: "00100011" ::
          "00100101" :: "00010101" :: "00000111" :: "00001000" :: "00000001" :: "00000010" :: "00000011" :: Nil
        }
      }

      "read" should {
        "empty" in {
          read(Nil) must_== Nil
        }

        "empty pieces" in {
          read("00000000" :: "00000000" :: Nil) must_== List(EastSide -> OpenSerie(Nil, 0))
        }

        "one serie" in {
          read("00010001" :: "00001100" :: "00001100" :: Nil) must_== List(WestSide -> OpenSerie(List(R12), 12))
        }

        "more series" in {
          read(
            "00000010" :: "00000011" :: "00000001" :: "00000010" ::
            "00000100" :: "00001010" :: "00010001" :: "00010010" :: "00010011" :: "00010100" ::
            "00010001" :: "00001100" :: "00001100" ::
            "00110001" :: "00000011" :: "00100011" ::
            "00100101" :: "00010101" :: "00000111" :: "00001000" :: "00000001" :: "00000010" :: "00000011" :: Nil) must_== (List(
              EastSide -> OpenSerie(List(R1, R2), 3),
              EastSide -> OpenSerie(List(L1, L2, L3, L4), 10),
              WestSide -> OpenSerie(List(R12), 12),
              SouthSide -> OpenSerie(List(G3), 3),
              NorthSide -> OpenSerie(List(R7, R8, R1, R2, R3), 21)))
        }
      }
    }
    "pairs" should {
      def write(all: List[(Side, OpenPair)]): List[String] =
        (BinaryFormat.opener writePairs all).showBytes.split(',').toList
      def read(bytes: List[String]): List[(Side, OpenPair)] =
        BinaryFormat.opener.readPairs(ByteArray.parseBytes(bytes))

      "write" should {
        "empty pieces" in {
          write(List(EastSide -> OpenPair(Nil, 1))) must_== "00000000" :: Nil
          }
        "one pair" in {
          write(List(WestSide -> OpenPair(List(R1, R1), 1))) must_== "00000001" :: "00000001" :: "00000001" :: Nil
        }
        "more pairs" in {
          write(List(
            EastSide -> OpenPair(List(L1, L1), 1),
            WestSide -> OpenPair(List(R12, R12), 1),
            NorthSide -> OpenPair(List(B10, B10), 1),
            SouthSide -> OpenPair(List(G8, G8), 1))) must_==
          "00000000" :: "00010001" :: "00010001" ::
          "00000001" :: "00001100" :: "00001100" ::
          "00000010" :: "00111010" :: "00111010" ::
          "00000011" :: "00101000" :: "00101000" :: Nil
        }
        }
      "read" in {
        "empty" in {
          read(Nil) must_== Nil
        }
        "more pairs" in {
          read("00000000" :: "00010001" :: "00010001" ::
            "00000001" :: "00001100" :: "00001100" ::
            "00000010" :: "00111010" :: "00111010" ::
            "00000011" :: "00101000" :: "00101000" :: Nil) must_==
          (List(
            EastSide -> OpenPair(List(L1, L1), 1),
            WestSide -> OpenPair(List(R12, R12), 1),
            NorthSide -> OpenPair(List(B10, B10), 1),
            SouthSide -> OpenPair(List(G8, G8), 1)))
        }
      }
    }

    // "open state" should {
    //   def write(all: OpenState): List[String] =
    //     (BinaryFormat.opener writeState all).showBytes.split(',').toList
    //   def read(bytes: List[String]): OpenState =
    //     BinaryFormat.opener.readState(ByteArray.parseBytes(bytes))

    //   "old open" should {
    //     "write" in {
    //       "serie state" in {
    //         write(OldOpen(SerieScore(10))) must_== "00000000" :: "00001010" :: Nil
    //         write(OldOpen(SerieScore(0xaaa))) must_== "00001010" :: "10101010" :: Nil

    //       }
    //       "pair sate" in {
    //         write(OldOpen(PairScore(256))) must_== "00010001" :: "00000000" :: Nil
    //       }
    //     }

    //     "read" in {
    //       "serie state" in {
    //         read("00000000" :: "00001010" :: Nil) must_== OldOpen(SerieScore(10))
    //         read("00001010" :: "10101010" :: Nil) must_== OldOpen(SerieScore(0xaaa))
    //       }
    //       "pair sate" in {
    //         read("00010001" :: "00000000" :: Nil) must_== OldOpen(PairScore(256))
    //       }
    //     }
    //   }

    //   "new open" should {
    //     val boardSave = Board(List(R1, R2))
    //     val openerSave = Opener(
    //       series = List(OpenSerie(EastSide, Piece.<>(10))),
    //       pairs = List(OpenPair(EastSide, R1.w)),
    //       opens = Sides(Some(OldOpen(SerieScore(11))),
    //         None,
    //         None,
    //         None))
    //     val binaryBoardSave = "00000010" :: "00000001" :: "00000010" :: Nil
    //     val binarySeriesSave = "00000100" :: "00001010" :: "00011010" :: "00101010" :: "00111010" ::Nil
    //     val binaryPairsSave = "00000000" :: "00000001" :: "00000001" :: Nil
    //     val binaryOpensSave = "00000000" :: "00001011" :: List.fill(6)("11111111")

    //     val binaryOpenerSave = binaryBoardSave ::: binarySeriesSave ::: binaryPairsSave ::: binaryOpensSave


    //     "write" in {
    //       "serie state" in {
    //         write(NewOpen(
    //           score = SerieScore(10),
    //           boardSave = boardSave,
    //           openerSave = openerSave)) must_== "01000000" :: "00001010" :: Nil ::: binaryOpenerSave
    //       }
    //       "pair state" in {
    //         write(NewOpen(
    //           score = PairScore(10),
    //           boardSave = boardSave,
    //           openerSave = openerSave)) must_== "01010000" :: "00001010" :: Nil ::: binaryOpenerSave
    //       }
    //     }
    //     "read" in {
    //       read("01000000" :: "00001010" :: Nil ::: binaryOpenerSave) must_== NewOpen(score = SerieScore(10))
    //     }
    //  }
  }
}
