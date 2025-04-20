package readsgsheet.auth

import com.github.nscala_time.time.Imports.*

import java.time.Clock
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import sttp.client4.quick.*
import sttp.client4.Response
import sttp.model.StatusCode
import upickle.default.*

implicit val clock: Clock = Clock.systemUTC

case class GoogleAccessToken(scope: String, accessToken: String, expireTime: Long)

object GoogleAccessToken:
  def generateFromServiceAccount(serviceAccount: GoogleServiceAccount, scope: String): GoogleAccessToken =
    val signedJwt: String = generateSignedJwt(serviceAccount, scope)

    val response: Response[String] = quickRequest
      .post(uri"https://oauth2.googleapis.com/token")
      .body(Map("grant_type" -> "urn:ietf:params:oauth:grant-type:jwt-bearer", "assertion" -> signedJwt))
      .send()

    val currentTimeInSecond: Long = DateTime.now().getMillis / 1000
    val (accessToken, expireTime): (String, Long) = getTokenFromResponse(response, currentTimeInSecond)

    GoogleAccessToken(scope, accessToken, expireTime)

  private def generateSignedJwt(serviceAccount: GoogleServiceAccount, scope: String): String =
    val claim = JwtClaim(s"""{"scope": "$scope"}""")
      .by(serviceAccount.clientEmail)
      .about(serviceAccount.clientEmail)
      .to("https://oauth2.googleapis.com/token")
      .issuedNow
      .expiresIn(3600)

    Jwt.encode(claim, serviceAccount.barePrivateKey, JwtAlgorithm.RS256)

  def getTokenFromResponse(response: Response[String], currentTimeInSecond: Long): (String, Long) =
    response.code match
      case StatusCode.Ok =>
        val parsedResponseBody = read[Map[String, String]](response.body)
        if parsedResponseBody.keySet.contains("access_token") && parsedResponseBody.keySet.contains("expires_in") then
          (parsedResponseBody("access_token"), currentTimeInSecond + parsedResponseBody("expires_in").toLong)
        else
          throw IllegalArgumentException("The response body must have access_token and expires_in")
      case _ =>
        throw RuntimeException("To get the token, the response from google oauth must be OK")
