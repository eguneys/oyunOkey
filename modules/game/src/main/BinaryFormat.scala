package oyun.game

import okey.{ Player => OkeyPlayer, Board, Piece, Side, OpenState, OpenPair, OpenSerie, Opener, NewOpen, OldOpen, SerieScore, PairScore, Sides, Color }

import okey.Color._
import okey.{ EastSide, WestSide, NorthSide, SouthSide }

import oyun.db.ByteArray

object BinaryFormat {
  object piece {

    // private def convert4PieceTo3(a: Int, b: Int, c: Int, d: Int): Array[Byte] = {
    //   Array(a | (b >> 6), b << 2 | (c >> 4), c << 4 | d >> 2) map (_.toByte)
    // }
    // private def convert2PieceTo2(a: Int, b: Int): Array[Byte] = {
    //   Array(a | (b >> 6), b << 2) map(_.toByte)
    // }

    // def pieceInt(piece: Piece): Int = {
    //   val color = piece.color match {
    //     case Red => 0
    //     case Black => 1
    //     case Green => 2
    //     case Blue => 3
    //     case _ => 0
    //   }
    //   val number = piece.number

    //   ((color << 6) | ((number) << 2))
    // }

    // def write(piece: Piece): Byte = pieceInt(piece).toByte

    // def write(pieces: List[Piece]): ByteArray = {
    //   ByteArray((pieces map pieceInt).grouped(4) flatMap {
    //     case List(b1, b2, b3, b4) => convert4PieceTo3(b1, b2, b3, b4)
    //     case List(b1, b2, b3) => convert4PieceTo3(b1, b2, b3, 0)
    //     case List(b1, b2) => convert2PieceTo2(b1, b2)
    //     case List(b1) => Array(b1.toByte)
    //   } toArray)
    // }

    // def read(ba: Byte): Piece = ???
    // def read(ba: ByteArray): List[Piece] = {
    //   def splitInts(bytes: Array[Byte]) = bytes match {
    //     case Array(b1, b2, b3) => Nil
    //     case Array(b1, b2) => Nil
    //     case Array(b1) => Nil
    //     case _ => Nil
    //   }

    //   ba.value grouped(3) flatMap splitInts map { _ =>
    //     Piece.R1
    //   } toList
    // }

    def write(piece: Piece): Byte = pieceInt(piece).toByte

    def write(pieces: List[Piece]): ByteArray = ByteArray(pieces map write toArray)

    def read(ba: Int): Piece = intPiece(ba)
    def read(ba: ByteArray): List[Piece] = ba.value map toInt map read toList

    def readList(ba: List[Int]): List[Piece] = ba map read

    private def pieceInt(piece: Piece): Int = {
      (colorToInt(piece.color) << 4) | (piece.number)
    }

    private def intPiece(b: Int): Piece = {
      val color = intToColor(b >> 4)
      val number = (b & 0x0f)
      Piece(color, number)
    }

    private def colorToInt(color: Color) = color match {
      case Red => 0
      case Black => 1
      case Green => 2
      case Blue => 3
      case _ => 0
    }

    private def intToColor(int: Int) = int match {
      case 0 => Red
      case 1 => Black
      case 2 => Green
      case 3 => Blue
      case _ => Red
    }
  }

  object opener {

    def readSeriesHelper(ba: List[Int]): List[OpenSerie] = ba match {
      case head :: rest => {
        val side = intToSide((head >> 4))
        val length = (head & 0x0f)
        val pieces = piece.readList(rest take length)

        OpenSerie(side, pieces) :: readSeriesHelper(rest drop length)
      }
      case _ => Nil
    }

    def readSeries(ba: ByteArray): List[OpenSerie] = readSeriesHelper(ba.value map toInt toList)


    def readPairs(ba: ByteArray): List[OpenPair] = {
      ba.value grouped 3 map {
        case Array(b1, b2, b3) => {
          val side = intToSide(b1)
          val pieces = piece.readList(List(b2, b3))

          OpenPair(side, pieces)
        }
        case x => sys error s"BinaryFormat.readPairs.read invalid bytes: ${ba.showBytes}"
      } toList
    }

    private def sideToInt(side: Side): Int = side match {
      case EastSide => 0
      case WestSide => 1
      case NorthSide => 2
      case SouthSide => 3
    }

    private def intToSide(int: Int): Side = int match {
      case 0 => EastSide
      case 1 => WestSide
      case 2 => NorthSide
      case _ => SouthSide
    }

    def writeSerie(serie: OpenSerie): List[Byte] = {
      val side = sideToInt(serie.owner)
      val length = serie.pieces.length
      val pieces = piece.write(serie.pieces).value toList

      (((side << 4) | (length & 0x0f)) toByte) :: pieces
    }

    def writeSeries(series: List[OpenSerie]): ByteArray = ByteArray(series flatMap writeSerie toArray)

    def writePair(pair: OpenPair): List[Byte] = {
      val side = sideToInt(pair.owner)
      val pieces = piece.write(pair.pieces).value toList

      (side toByte) :: pieces
    }

    def writePairs(pairs: List[OpenPair]): ByteArray = ByteArray(pairs flatMap writePair toArray)


    def readState(ba: ByteArray): OpenState = ba.value map toInt toList match {
      case b1 :: b2 :: rest => {
        val s = (((b1 & 0x0f) << 8) | b2)
        val score = if ((b1 & 0x30) == 0) SerieScore(s) else PairScore(s)

        if ((b1 & 0xc0) == 0) {
          OldOpen(score)
        } else {
          val boardLength = (rest head)
          val board = (rest drop 1) take boardLength

          val rest2 = rest drop (boardLength + 1)
          val seriesLength = ((rest2 head) & 0x0f) + 1
          val series = rest2 take seriesLength

          val rest3 = rest2 drop seriesLength
          val pairs = (rest3 take 3)
          val states = (rest3 drop 3)

          val opens: Sides[Option[OpenState]] = (states grouped (2) map {
            case b1 :: b2 if (b1 == 0xff) => None
            case x => Some(readState(ByteArray((x.map(_.toByte) toArray))))
          } toList) match {
            case e :: w :: n :: s :: Nil =>
              Sides(e, w, n, s)
            case x => sys error s"BinaryFormat.readOpenState.read invalid bytes: ${ba.showBytes}"
          }

          val boardSave = Board(piece.readList(board))
          val openerSave = Opener(
            series = readSeries(ByteArray(series map(_.toByte) toArray)),
            pairs = readPairs(ByteArray(pairs map(_.toByte) toArray)),
            opens = opens)

          NewOpen(score = score, boardSave = boardSave, openerSave = openerSave)
        }
      }
      case x => sys error s"BinaryFormat.readOpenState.read invalid bytes: ${ba.showBytes}"
    }

    private def writeScore(old: Int, serie: Int, score: Int): List[Int] = {
      List((old << 6) | (serie << 4) | (score & 0xfff) >> 8, (score & 0xff))
    }

    private def writeSave(board: Board, opener: Opener): List[Int] = {
      val pieces = piece.write(board.pieceList).value map (_.toInt) toList
      val series = writeSeries(opener.series).value map (_.toInt) toList
      val pairs =  writePairs(opener.pairs).value map (_.toInt) toList

      val states = (opener.opens map {
        case Some(a: OldOpen) => writeOldState(a).value map (_.toInt) toList
        case _ => List(0xff, 0xff)
      }).toList flatten

      pieces.length :: pieces ::: series ::: pairs ::: states
    }

    def writeState(state: OpenState): ByteArray = state match {
      case o:OldOpen => writeOldState(o)
      case o:NewOpen => writeNewState(o)
    }

    def writeOldState(state: OldOpen): ByteArray = ByteArray((state match {
      case OldOpen(SerieScore(s)) => writeScore(0, 0, s)
      case OldOpen(PairScore(s)) => writeScore(0, 1, s)
    }) map (_.toByte) toArray)

    def writeNewState(state: NewOpen): ByteArray = ByteArray((state match {
      case NewOpen(SerieScore(s), board, opener) => writeScore(1, 0, s) ::: writeSave(board, opener)
      case NewOpen(PairScore(s), board, opener) => writeScore(1, 1, s) ::: writeSave(board, opener)
    }) map (_.toByte) toArray)
  }

  object player {
    def read(ba: ByteArray): (Option[Piece], Boolean) = ba.value map toInt match {
      case Array(b1) =>
        val middle = (b1 & 0x80) == 0x80
        val left = (b1 & 0x40) == 0x40

        val oleft = left.fold(None, {
          Some(piece.read(b1 & 0x3f))
        })

        (oleft, middle)

      case x => sys error s"BinaryFormat.readPlayer.read invalid bytes: ${ba.showBytes}"
    }

    def write(player: OkeyPlayer): ByteArray = ByteArray({
      val middle = player.drawMiddle.fold(1, 0)
      val left = player.drawLeft.isDefined.fold(0, 1)

      // assume piece don't use two bytes left
      val oleft = player.drawLeft.fold(0x3f) { p =>
        piece.write(p)
      }

      Array((middle << 7) | (left << 6) | oleft) map(_.toByte)
    })
  }

  @inline private def toInt(b: Byte): Int = b & 0xff
}
