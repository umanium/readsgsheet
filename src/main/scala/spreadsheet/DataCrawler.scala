package spreadsheet

import auth.{GoogleAccessToken, GoogleServiceAccount}
import sttp.client4.Response
import sttp.client4.quick.*
import sttp.model.StatusCode

import scala.annotation.tailrec

case class DataCrawler(spreadsheet: Spreadsheet, serviceAccount: GoogleServiceAccount):
  @tailrec
  final def getData(startsAt: Cell, header: Seq[String], readWindow: Int, continuous: Boolean = false,
              initialData: Seq[Seq[String]] = Seq(), accessToken: Option[GoogleAccessToken] = None): Seq[Seq[String]] =
    val googleAccessToken: GoogleAccessToken = accessToken match
      case Some(token: GoogleAccessToken) => token
      case None => GoogleAccessToken.generateFromServiceAccount(serviceAccount, "https://www.googleapis.com/auth/spreadsheets")

    val range: String = DataCrawler.getTableRange(startsAt, header, readWindow)

    val response: Response[String] = quickRequest
      .get(uri"https://sheets.googleapis.com/v4/spreadsheets/${spreadsheet.sheetId}/values/${spreadsheet.sheetName}!$range")
      .auth.bearer(googleAccessToken.accessToken)
      .send()

    val addedData: Seq[Seq[String]] = DataCrawler.getDataFromResponse(response)
    if(addedData.nonEmpty && continuous)
      if(addedData.length < readWindow)
        initialData ++ addedData
      else
        getData(Cell(startsAt.column, startsAt.row + readWindow), header, readWindow,
          continuous, initialData ++ addedData, Some(googleAccessToken))
    else if(addedData.nonEmpty)
      initialData ++ addedData
    else
      initialData

  @tailrec
  final def getHeader(startsAt: Cell, readWindow: Int, initialData: Seq[String] = Seq(), accessToken: Option[GoogleAccessToken] = None): Seq[String] =
    val googleAccessToken: GoogleAccessToken = accessToken match
      case Some(token: GoogleAccessToken) => token
      case None => GoogleAccessToken.generateFromServiceAccount(serviceAccount, "https://www.googleapis.com/auth/spreadsheets")

    val endsAt: Cell = Cell(startsAt.column + readWindow - 1, startsAt.row)
    val range: String = s"${Cell.getCellString(startsAt)}:${Cell.getCellString(endsAt)}"

    val response: Response[String] = quickRequest
      .get(uri"https://sheets.googleapis.com/v4/spreadsheets/${spreadsheet.sheetId}/values/${spreadsheet.sheetName}!$range")
      .auth.bearer(googleAccessToken.accessToken)
      .send()

    val addedData: Seq[Seq[String]] = DataCrawler.getDataFromResponse(response)
    if(addedData.nonEmpty)
      if(addedData.head.length < readWindow)
        initialData ++ addedData.head
      else
        val nextStart: Cell = Cell(startsAt.column + readWindow, startsAt.row)
        getHeader(nextStart, readWindow, initialData ++ addedData.head, Some(googleAccessToken))
    else
      initialData

object DataCrawler:
  def getTableRange(startsAt: Cell, headers: Seq[String], readWindow: Int): String =
    val endColumn: Int = startsAt.column + headers.length - 1
    val endRow: Int = startsAt.row + readWindow - 1
    val endsAt: Cell = Cell(endColumn, endRow)
    s"${Cell.getCellString(startsAt)}:${Cell.getCellString(endsAt)}"

  def getDataFromResponse(response: Response[String]): Seq[Seq[String]] =
    response.code match
      case StatusCode.Ok =>
        val parsedResponseBody = ujson.read(response.body)
        if(parsedResponseBody.obj.keys.toList.contains("values"))
          val valueArray = parsedResponseBody("values").arr
          valueArray.toList.map(a => a.arr.toList.map(v => v.str))
        else
          Seq()
      case _ =>
        throw RuntimeException(s"To get the data, the response from gsheet API must be OK.\nCode: ${response.code}\nBody: ${response.body}")
