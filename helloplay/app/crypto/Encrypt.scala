package crypto

import play.api.libs.Crypto

object Encrypt {

  def encrypt(clearText: String): String = {
    Crypto.encryptAES(clearText)
  }

  def decrypt(encrypted: String): String = {
    Crypto.decryptAES(encrypted)
  }

}