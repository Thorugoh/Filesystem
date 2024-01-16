import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.nio.file.Files
import java.nio.file.Paths
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.streams.toList

const val FOLDER = "C:\\Users\\thoru\\Documents"
const val TRASH = "C:\\Users\\thoru\\Trash"

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

            get("/files/{name}"){
                val filename = call.parameters["name"]
                if(filename != null) {
                    val decryptedStream = FileShareManager.decryptFile(filename)
                    if(decryptedStream != null){
                        call.response.header(
                            HttpHeaders.ContentDisposition,
                            ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, filename).toString()
                        )
                        call.respondOutputStream {
                            decryptedStream.copyTo(this)
                        }
                    } else {
                        call.respond(HttpStatusCode.NotFound, "File not found or decryption failed.")
                    }
                }else {
                    call.respond(HttpStatusCode.BadRequest, "Invalid file name")
                }
            }

            post("/files") {
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    if(part is PartData.FileItem) {
                        println(part)
                        val name = part.originalFileName!!
                        part.streamProvider().use { its ->
                            if(FileShareManager.createFile(name, inputStream = its)) {
                                println("File created: $name")
                            } else {
                                println("Failed to create file $name")
                            }
                        }
                    }
                    part.dispose()
                }
            }

            delete("/files/{name}") {
                val filename = call.parameters["name"]!!
                if(FileShareManager.deleteFile(filename)) {
                    call.respond(HttpStatusCode.OK)
                }
                else call.respond(HttpStatusCode.NotFound)
            }

            post("/files/{name}/restore") {
                val filename = call.parameters["name"]!!
                if(FileShareManager.restoreFile(filename)) {
                    call.respond(HttpStatusCode.OK)
                }
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }.start(wait = true)
}