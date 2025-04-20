package readsgsheet.auth

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should
import readsgsheet.auth.GoogleServiceAccount
import upickle.core.AbortException

import java.io.FileNotFoundException

class GoogleServiceAccountTest extends AnyFunSpec with should.Matchers:
  describe("read a service account file"):
    it("should return a valid ServiceAccount on reading the valid file"):
      val fileName: String = "service_account_good.json"
      val filePath: String = getClass.getResource(s"/auth/$fileName").getPath

      val expectedServiceAccount: GoogleServiceAccount = GoogleServiceAccount("a-project", "a_private_key_id", "a_private_key", "a_client_email",
        "1234567890")
      val actualServiceAccount = GoogleServiceAccount.fromJson(filePath)
      actualServiceAccount shouldEqual expectedServiceAccount

    it("should throw an exception when the file is not available"):
      val fileName: String = "service_account_not_available.json"

      an[FileNotFoundException] should be thrownBy GoogleServiceAccount.fromJson(fileName)

    it("should throw an exception when the file has a bad json format"):
      val fileName: String = "service_account_bad_json_format.json"
      val filePath: String = getClass.getResource(s"/auth/$fileName").getPath

      an[AbortException] should be thrownBy GoogleServiceAccount.fromJson(filePath)
