import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

object FileShareManager {
    private val crypto = Crypto();

    fun listFiles(): List<String> {
        val folderPath = Paths.get(FOLDER)

        return Files.list(folderPath)
            .filter { Files.isRegularFile(it) }
            .map { path -> path.fileName.toString() }
            .toList();
    }

    fun deleteFile(filename: String): Boolean {
        val file = java.io.File("$FOLDER\\$filename")

        if(file.exists()) {
            file.copyTo(java.io.File("$TRASH\\$filename"))
            file.delete()
            return true;
        }
        return false;
    }

    fun createFile(name: String, inputStream: InputStream): Boolean {
        return try{
            val file = java.io.File("$FOLDER\\$name")
                file.outputStream().use { outputStream ->
                    crypto.encryptStream(inputStream, outputStream)
                }
            true
        } catch (e: java.lang.Exception) {
            println("Error while creating file: ${e.message}")
            false
        }
    }

    fun decryptFile(name: String): InputStream? {
        return try {
            val file = java.io.File("$FOLDER\\$name")
            val encryptedInputStream = FileInputStream(file)
            val decryptedStream = ByteArrayOutputStream()
            crypto.decryptStream(encryptedInputStream, decryptedStream)
            ByteArrayInputStream(decryptedStream.toByteArray())
        }catch(e: Exception) {
            println("Error while decrypting file: ${e.message}")
            null
        }
    }

    fun restoreFile(filename: String): Boolean {
        val file = java.io.File("$TRASH\\$filename")
        if(file.exists())
        {
            file.copyTo(java.io.File("$FOLDER\\$filename"))
            file.delete()
            return true;
        }
        return false
    }
}
