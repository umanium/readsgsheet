package readsgsheet.auth

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should
import com.github.nscala_time.time.Imports.*
import readsgsheet.auth.GoogleAccessToken
import sttp.client4.{Response, UriContext}
import sttp.model.{Method, RequestMetadata, StatusCode}

class GoogleAccessTokenTest extends AnyFunSpec with should.Matchers:
  describe("getting token from response"):
    val responseBody: String =
      """{
        |  "access_token":"example_access_token",
        |  "expires_in":3599,
        |  "token_type":"Bearer"
        |}""".stripMargin

    val requestMetadata: RequestMetadata = RequestMetadata(Method(""), uri"http://localhost", Seq())

    it("should return the access token and expire time"):
      val currentTimeSecond = DateTime.now().getMillis / 1000
      val response: Response[String] = Response(responseBody, StatusCode.Ok, requestMetadata)

      val expectedOutput: (String, Long) = ("example_access_token", currentTimeSecond + 3599)
      val actualOutput = GoogleAccessToken.getTokenFromResponse(response, currentTimeSecond)

      actualOutput shouldEqual expectedOutput

    it("should throw a RuntimeException if the response status code is not OK"):
      val currentTimeSecond = DateTime.now().getMillis / 1000
      val response: Response[String] = Response(responseBody, StatusCode.BadRequest, requestMetadata)

      an[RuntimeException] should be thrownBy GoogleAccessToken.getTokenFromResponse(response, currentTimeSecond)

    it("should throw an IllegalArgumentException if the response body has incomplete field"):
      val currentTimeSecond = DateTime.now().getMillis / 1000
      val responseNoExpiresIn: Response[String] = Response("""{"access_token": "incomplete"}""", StatusCode.Ok, requestMetadata)
      
      an[IllegalArgumentException] should be thrownBy GoogleAccessToken.getTokenFromResponse(responseNoExpiresIn, currentTimeSecond)
