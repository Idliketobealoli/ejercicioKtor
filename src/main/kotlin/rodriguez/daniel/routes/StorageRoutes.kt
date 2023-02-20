package rodriguez.daniel.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import rodriguez.daniel.model.Role
import rodriguez.daniel.services.storage.StorageService
import rodriguez.daniel.services.user.UserService
import java.time.LocalDateTime
import java.util.*

private const val ENDPOINT = "ejercicioKtor/storage"

fun Application.storageRoutes() {
    val storage: StorageService by inject()
    val users: UserService by inject()

    routing {
        route("/$ENDPOINT") {
            get("check") {
                println("GET ALL /$ENDPOINT/check")
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "status" to "OK",
                        "message" to "Ejercicio de ktor para practicar. Daniel Rodriguez M",
                        "createdAt" to LocalDateTime.now().toString()
                    )
                )
            }

            post {
                println("POST /$ENDPOINT")
                try {
                    val readChannel = call.receiveChannel()
                    val fileName = UUID.randomUUID().toString()
                    val res = storage.saveFile(fileName, readChannel)
                    if (res != null) call.respond(HttpStatusCode.OK, res)
                    else call.respond(HttpStatusCode.InternalServerError, "could not save in $fileName.")
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}")
                }
            }

            get("{fileName}") {
                println("GET /$ENDPOINT/{fileName}")
                val fileName = call.parameters["fileName"].toString()
                val file = storage.getFile(fileName)
                if (file != null) call.respondFile(file)
                else call.respond(HttpStatusCode.NotFound, "file with name $fileName not found")
            }

            authenticate {
                delete("{fileName}") {
                    println("DELETE /$ENDPOINT/{fileName}")
                    val jwt = call.principal<JWTPrincipal>()
                    val userId = jwt?.payload?.getClaim("id")
                        .toString().replace("\"", "")
                    val user = users.findUserById(UUID.fromString(userId.trim()))
                    if (user != null) {
                        if (user.role == Role.ADMIN) {
                            val fileName = call.parameters["fileName"].toString()
                            val deleted = storage.deleteFile(fileName)
                            if (deleted == null) call.respond(HttpStatusCode.NotFound, "file with name $fileName not found")
                            else call.respond(HttpStatusCode.NoContent)
                        }
                        else call.respond(HttpStatusCode.Forbidden, "You can't delete this.")
                    }
                    else call.respond(HttpStatusCode.Unauthorized, "Could not authenticate. User not found.")
                }
            }
        }
    }
}