package spreadsheet

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should

class CellTest extends AnyFunSpec with should.Matchers:
  describe("fromCellString"):
    it("should parse Excel cell string (e.g. A1, Z34, AB27)"):
      Cell.fromCellString("A1") shouldEqual Cell(1, 1)
      Cell.fromCellString("Z34") shouldEqual Cell(26, 34)
      Cell.fromCellString("BE873") shouldEqual Cell(57, 873)
      Cell.fromCellString("AAA9999") shouldEqual Cell(703, 9999)

    it("should throw an IllegalArgumentException if the cell string is malformed"):
      an[IllegalArgumentException] should be thrownBy Cell.fromCellString("35")
      an[IllegalArgumentException] should be thrownBy Cell.fromCellString("BAC")
      an[IllegalArgumentException] should be thrownBy Cell.fromCellString("39IDN")

  describe("getColumnInt"):
    it("should get int representation of a column string"):
      Cell.getColumnInt("A") shouldEqual 1
      Cell.getColumnInt("Z") shouldEqual 26
      Cell.getColumnInt("GA") shouldEqual 183
      Cell.getColumnInt("AAA") shouldEqual 703

  describe("getCellString"):
    it("should get cell string from a cell object"):
      Cell.getCellString(Cell(1,1)) shouldEqual "A1"
      Cell.getCellString(Cell(57, 873)) shouldEqual "BE873"
      Cell.getCellString(Cell(703, 1572)) shouldEqual "AAA1572"

  describe("getColumnString"):
    it("should get string representation of a column int"):
      Cell.getColumnString(1) shouldEqual "A"
      Cell.getColumnString(26) shouldEqual "Z"
      Cell.getColumnString(57) shouldEqual "BE"
      Cell.getColumnString(183) shouldEqual "GA"
      Cell.getColumnString(703) shouldEqual "AAA"
