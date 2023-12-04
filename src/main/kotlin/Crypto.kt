import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey

class Crypto() {
    private val cipher: Cipher = Cipher.getInstance("AES")

    fun encrypt(content: String,  secretKey: SecretKey): String {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(content.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(content: String,  secretKey: SecretKey): String {
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(content))
        return String(decryptedBytes)
    }
}
