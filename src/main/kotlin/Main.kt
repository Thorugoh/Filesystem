import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.streams.toList

const val FOLDER = "C:\\Users\\thoru\\Documents"
const val TRASH = "C:\\Users\\thoru\\Trash"

object FileShareManager {
    private val keyGenerator = KeyGenerator.getInstance("AES").apply { init(128) }

    fun listFiles(): List<String> {
       val folderPath = Paths.get(FOLDER)
        val files = Files.list(folderPath)
            .filter { Files.isRegularFile(it) }
            .map { path -> path.fileName.toString() }
            .toList()

        return files;
    }

}

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/files") {
                call.respondText(
                    text = FileShareManager.listFiles().toString(),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.OK
                )
            }

            post("/files") {
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    if(part is PartData.FileItem) {
                        println(part)
                        val name = part.originalFileName!!
                        val file = java.io.File("$FOLDER\\$name")

                        part.streamProvider().use { its ->
                            file.outputStream().use {
                                its.copyTo(it)
                            }
                        }
                    }
                    part.dispose()
                }
            }

            delete("/files/{name}") {
                val filename = call.parameters["name"]!!
                val file = java.io.File("$FOLDER\\$filename")
                if(file.exists()) {
                    file.copyTo(java.io.File("$TRASH\\$filename"))
                    file.delete()
                    call.respond(HttpStatusCode.OK)
                }
                else call.respond(HttpStatusCode.NotFound)
            }

            post("/files/{name}/restore") {
                val filename = call.parameters["name"]!!
                val file = java.io.File("$TRASH\\$filename")
                if(file.exists())
                {
                    file.copyTo(java.io.File("$FOLDER\\$filename"))
                    file.delete()
                    call.respond(HttpStatusCode.OK)
                }
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }.start(wait = true)
}