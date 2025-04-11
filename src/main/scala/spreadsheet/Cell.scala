package spreadsheet

import scala.annotation.tailrec
import scala.util.matching.Regex

case class Cell(column: Int, row: Int)

object Cell:
  def getColumnInt(colStr: String): Int =
    colStr.reverse.zipWithIndex.foldLeft(0)((prev, c) => prev + ((c._1.toInt - 64) * scala.math.round(scala.math.pow(26, c._2)).toInt))

  def fromCellString(cellString: String): Cell =
    val numberPattern: Regex = "([A-Z]+)([0-9]+)".r
    val matched: Option[Regex.Match] = numberPattern.findFirstMatchIn(cellString)
    matched match
      case Some(m: Regex.Match) =>
        val colStr: String = m.group(1)
        val colInt: Int = getColumnInt(colStr)
        val rowInt: Int = m.group(2).toInt

        Cell(colInt, rowInt)

      case None =>
        throw IllegalArgumentException(s"The cell '$cellString' is not valid")

  @tailrec
  def getColumnString(colInt: Int, initString: String = ""): String =
    val nextPow: Int = scala.math.pow(26, initString.length + 1).toInt
    val toCalculate: Int = if(colInt % nextPow == 0) 26 else colInt % nextPow

    val basePow: Int = scala.math.pow(26, initString.length).toInt
    val currentInt: Int = toCalculate / basePow
    val currentChar: Char = (currentInt + 64).toChar
    val nextString: String = currentChar + initString

    val nextCalculation: Int = colInt - (basePow * currentInt)
    if(nextCalculation > 0)
      getColumnString(nextCalculation, nextString)
    else
      nextString

  def getCellString(cell: Cell): String =
    val colStr: String = getColumnString(cell.column)
    val rowStr: String = cell.row.toString

    s"$colStr$rowStr"
