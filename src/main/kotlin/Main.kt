import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

open class File(val name: String, var content: String) {
    override fun toString(): String {
        return name;
    }
}

object FileShareManager {
    private val files = mutableMapOf<String, File>()
    private val deletedFiles = mutableMapOf<String, File>()
    private val keyGenerator = KeyGenerator.getInstance("AES").apply { init(128) }
    private val secretKey: SecretKey = keyGenerator.generateKey()
    private val crypto = Crypto();

    fun saveFile(file: File) {
        val encryptedFile = File(file.name, crypto.encrypt(file.content, secretKey));
        files[file.name] = encryptedFile
    }

    fun restoreFile(name: String) : File? {
        return deletedFiles[name]?.also {
            files[name] = it
            deletedFiles.remove(name)
        }
    }

    fun deleteFile(name: String) {
        files[name]?.also {
            deletedFiles[name] = it
            files.remove(name)
        }
    }

    fun listFiles(): List<File> {
        return files.values.toList()
    }

    fun searchFiles(query: String): List<File> {
        return files.values.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun readFile(query: String) {
        val files = searchFiles(query)
        if(files.size == 1){
            println(crypto.decrypt(files[0].content, secretKey))
        }
    }
}

fun main(args: Array<String>) {
    val file1 = File("doc1", "Hello World")
    FileShareManager.saveFile(file1)

    val file2 = File("doc2", "Some Content")
    FileShareManager.saveFile(file2)

    println(FileShareManager.listFiles())
    FileShareManager.deleteFile("doc1")
    println(FileShareManager.listFiles())

    FileShareManager.restoreFile("doc1")
    println(FileShareManager.listFiles())

    FileShareManager.readFile("doc1")
}