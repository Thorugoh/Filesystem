import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.util.Base64
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

private const val ALGORITHM = "AES"
private const val KEY_SIZE = 128

class Crypto() {
    private val cipher: Cipher = Cipher.getInstance(ALGORITHM)

    private fun getKey(): SecretKey {
        val props = Properties()
        FileInputStream("C:\\Users\\thoru\\Documents\\config.properties").use { input ->
            props.load(input)
        }
        val keyStr = props.getProperty("encryption.key") ?: throw IllegalStateException("Encryption key not found")
        val decodedKey = Base64.getDecoder().decode(keyStr)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, ALGORITHM)
    }

    fun encryptStream(inputStream: InputStream, outputStream: OutputStream,  secretKey: SecretKey = getKey()) {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        CipherOutputStream(outputStream, cipher).use { cos ->
            inputStream.copyTo(cos)
        }
    }

    fun decryptStream(inputStream: InputStream, outputStream: OutputStream,  secretKey: SecretKey = getKey()) {
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        CipherInputStream(inputStream, cipher).use { cis ->
            cis.copyTo(outputStream)
        }
    }

    fun generateKey(): Key {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM).apply { init(KEY_SIZE) }
        return keyGenerator.generateKey()
    }
}
