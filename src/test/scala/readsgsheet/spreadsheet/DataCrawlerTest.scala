package readsgsheet.spreadsheet

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should
import readsgsheet.spreadsheet.{Cell, DataCrawler}
import sttp.client4.{Response, UriContext}
import sttp.model.{Method, RequestMetadata, StatusCode}

class DataCrawlerTest extends AnyFunSpec with should.Matchers:
  describe("getDataFromResponse"):
    val requestMetadata = RequestMetadata(Method(""), uri"http://localhost", Seq()): RequestMetadata
    it("should return the first values in array of string"):
      val responseString: String =
        """
          |{
          |  "range": "'Form Responses 1'!A290:E302",
          |  "majorDimension": "ROWS",
          |  "values": [
          |    [
          |      "4/6/2025 18:56:53",
          |      "4/6/2025",
          |      "79.98",
          |      "Lei",
          |      "Decathlon"
          |    ],
          |    [
          |      "4/6/2025 18:57:09",
          |      "4/6/2025",
          |      "24.25",
          |      "Lei",
          |      "Vitamin"
          |    ]
          |  ]
          |}
          |""".stripMargin
      val response: Response[String] = Response(responseString, StatusCode.Ok, requestMetadata)
      val expectedResult: Seq[Seq[String]] = Seq(
        Seq("4/6/2025 18:56:53", "4/6/2025", "79.98", "Lei", "Decathlon"),
        Seq("4/6/2025 18:57:09", "4/6/2025", "24.25", "Lei", "Vitamin")
      )

      DataCrawler.getDataFromResponse(response) shouldEqual expectedResult

    it("should throw a RuntimeException if the response status code is not OK"):
      val response: Response[String] = Response("", StatusCode.BadRequest, requestMetadata)

      an[RuntimeException] should be thrownBy DataCrawler.getDataFromResponse(response)

    it("should return empty array if there is no 'values' field in the response"):
      val responseString: String =
        """
          |{
          |  "range": "'Form Responses 1'!A290:E302",
          |  "majorDimension": "ROWS"
          |}
          |""".stripMargin
      val response: Response[String] = Response(responseString, StatusCode.Ok, requestMetadata)

      DataCrawler.getDataFromResponse(response) shouldEqual Seq()

  describe("getTableRange"):
    it("will get sheet range given startsAt cell and the headers"):
      val startsAt: Cell = Cell(3,4)
      val headers: Seq[String] = Seq("ID", "Name", "Age")

      val expectedRange: String = "C4:E4"
      val expectedRangeWithWindow: String = "C4:E8"

      DataCrawler.getTableRange(startsAt, headers, 1) shouldEqual expectedRange
      DataCrawler.getTableRange(startsAt, headers, 5) shouldEqual expectedRangeWithWindow
