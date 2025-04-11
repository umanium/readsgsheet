package auth

import upickle.default.*

import scala.io.{BufferedSource, Source}

case class GoogleServiceAccount(projectId: String, privateKeyId: String, privateKey: String, clientEmail: String,
                                clientId: String):
  def barePrivateKey: String = privateKey
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replaceAll("\\s+","")

object GoogleServiceAccount:
  def fromJson(jsonFilePath: String): GoogleServiceAccount =
    val fileSource: BufferedSource = Source.fromFile(jsonFilePath)
    val fileContent: String = fileSource.mkString

    val contentMap: Map[String, String] = read[Map[String, String]](fileContent)

    // get values
    val projectId: String = contentMap("project_id")
    val privateKeyId: String = contentMap("private_key_id")
    val privateKey: String = contentMap("private_key")
    val clientEmail: String = contentMap("client_email")
    val clientId: String = contentMap("client_id")

    GoogleServiceAccount(projectId, privateKeyId, privateKey, clientEmail, clientId)
