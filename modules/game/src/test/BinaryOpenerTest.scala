package oyun.game

import org.specs2.mutable._

import oyun.db.ByteArray

import okey._

import Piece._

class BinaryOpenerTest extends Specification {

  val noop = "00000000"

  "binary opener" should {
    "series" should {
      def write(all: List[OpenSerie]): List[String] =
        (BinaryFormat.opener writeSeries all).showBytes.split(',').toList
      def read(bytes: List[String]): List[OpenSerie] =
        BinaryFormat.opener.readSeries(ByteArray.parseBytes(bytes))
      "write" should {
        "empty opens" in {
          write(List.empty) must_== "" :: Nil
        }
        "empty pieces" in {
          write(List(OpenSerie(EastSide, Nil))) must_== "00000000" :: Nil
          write(List(OpenSerie(WestSide, Nil))) must_== "00010000" :: Nil
          write(List(OpenSerie(NorthSide, Nil))) must_== "00100000" :: Nil
          write(List(OpenSerie(SouthSide, Nil))) must_== "00110000" :: Nil
        }
        "more pieces" in {
          write(List(OpenSerie(EastSide, List(R1, R2)),
            OpenSerie(EastSide, List(L1, L2, L3, L4)),
            OpenSerie(WestSide, List(R12)),
            OpenSerie(SouthSide, List(G3)),
            OpenSerie(NorthSide, List(R7, R8, R1, R2, R3)))) must_==
          "00000010" :: "00000001" :: "00000010" ::
          "00000100" :: "00010001" :: "00010010" :: "00010011" :: "00010100" ::
          "00010001" :: "00001100" ::
          "00110001" :: "00100011" ::
          "00100101" :: "00000111" :: "00001000" :: "00000001" :: "00000010" :: "00000011" :: Nil
        }
      }

      "read" should {
        "empty" in {
          read(Nil) must_== Nil
        }

        "empty pieces" in {
          read("00000000" :: Nil) must_== List(OpenSerie(EastSide, Nil))
        }

        "one serie" in {
          read("00010001" :: "00001100" :: Nil) must_== List(OpenSerie(WestSide, List(R12)))
        }

        "more series" in {
          read("00000010" :: "00000001" :: "00000010" ::
            "00000100" :: "00010001" :: "00010010" :: "00010011" :: "00010100" ::
            "00010001" :: "00001100" ::
            "00110001" :: "00100011" ::
            "00100101" :: "00000111" :: "00001000" :: "00000001" :: "00000010" :: "00000011" :: Nil) must_== (List(OpenSerie(EastSide, List(R1, R2)),
            OpenSerie(EastSide, List(L1, L2, L3, L4)),
            OpenSerie(WestSide, List(R12)),
            OpenSerie(SouthSide, List(G3)),
            OpenSerie(NorthSide, List(R7, R8, R1, R2, R3))))
        }
      }
    }
    "pairs" should {
      def write(all: List[OpenPair]): List[String] =
        (BinaryFormat.opener writePairs all).showBytes.split(',').toList
      def read(bytes: List[String]): List[OpenPair] =
        BinaryFormat.opener.readPairs(ByteArray.parseBytes(bytes))

      "write" should {
        "empty pieces" in {
          write(List(OpenPair(EastSide, Nil))) must_== "00000000" :: Nil
        }
        "one pair" in {
          write(List(OpenPair(WestSide, List(R1, R1)))) must_== "00000001" :: "00000001" :: "00000001" :: Nil
        }
        "more pairs" in {
          write(List(
            OpenPair(EastSide, List(L1, L1)),
            OpenPair(WestSide, List(R12, R12)),
            OpenPair(NorthSide, List(B10, B10)),
            OpenPair(SouthSide, List(G8, G8)))) must_==
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
            OpenPair(EastSide, List(L1, L1)),
            OpenPair(WestSide, List(R12, R12)),
            OpenPair(NorthSide, List(B10, B10)),
            OpenPair(SouthSide, List(G8, G8))))
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
