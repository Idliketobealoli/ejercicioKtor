package rodriguez.daniel.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import rodriguez.daniel.dto.EmpleadoDTOcreacion
import rodriguez.daniel.exception.DepartamentoExceptionNotFound
import rodriguez.daniel.exception.EmpleadoExceptionBadRequest
import rodriguez.daniel.exception.EmpleadoExceptionNotFound
import rodriguez.daniel.model.Role
import rodriguez.daniel.services.empleado.EmpleadoService
import rodriguez.daniel.services.storage.StorageService
import rodriguez.daniel.services.user.UserService
import java.util.*

private const val ENDPOINT = "ejercicioKtor/empleados"

fun Application.empleadoRoutes() {
    val empleados: EmpleadoService by inject()
    val users: UserService by inject()
    val storage: StorageService by inject()

    routing {
        route("/$ENDPOINT") {
            get {
                val res = empleados.findAllEmpleados()
                if (res.isEmpty()) call.respond(HttpStatusCode.NotFound, res)
                else call.respond(HttpStatusCode.OK, res)
            }

            get("{id}") {
                try {
                    val id = UUID.fromString(call.parameters["id"])
                    val res = empleados.findEmpleadoById(id)
                    call.respond(HttpStatusCode.OK, res)
                } catch (e: EmpleadoExceptionNotFound) {
                    call.respond(HttpStatusCode.NotFound, e.message.toString())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }
            }

            post {
                try {
                    val dep = call.receive<EmpleadoDTOcreacion>()
                    val res = empleados.saveEmpleado(dep)
                    call.respond(HttpStatusCode.Created, res)
                } catch (e: DepartamentoExceptionNotFound) {
                    call.respond(HttpStatusCode.NotFound, e.message.toString())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }
            }

            // este es solo para cambiar el avatar del empleado
            put("{id}") {
                try {
                    val readChannel = call.receiveChannel()
                    val fileName = UUID.randomUUID().toString()
                    val saved = storage.saveFile(fileName, readChannel)
                    if (saved == null)
                        call.respond(HttpStatusCode.BadRequest, "Unable to save file or it is not a valid one.")
                    else {
                        if (saved["fileName"] == null)
                            call.respond(HttpStatusCode.BadRequest, "Unable to save file or it is not a valid one.")
                        else {
                            val id = UUID.fromString(call.parameters["id"])
                            val previous = empleados.findEmpleadoById(id)
                            val newAvatar = saved["fileName"] ?: previous.avatar
                            val new = EmpleadoDTOcreacion(
                                id, previous.nombre, previous.email,
                                newAvatar, previous.departamentoId
                            )
                            val res = empleados.saveEmpleado(new)
                            call.respond(HttpStatusCode.Created, res)
                        }
                    }
                } catch (e: EmpleadoExceptionNotFound) {
                    call.respond(HttpStatusCode.NotFound, e.message.toString())
                } catch (e: DepartamentoExceptionNotFound) {
                    call.respond(HttpStatusCode.NotFound, e.message.toString())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }
            }

            delete("{id}") {
                try {
                    val jwt = call.principal<JWTPrincipal>()
                    val userId = jwt?.payload?.getClaim("id")
                        .toString().replace("\"", "")
                    val user = users.findUserById(UUID.fromString(userId.trim()))
                    if (user != null) {
                        if (user.role == Role.ADMIN) {
                            val id = UUID.fromString(call.parameters["id"])
                            val res = empleados.deleteEmpleado(id)
                            call.respond(HttpStatusCode.OK, res)
                        } else call.respond(HttpStatusCode.Forbidden, "You can't consult all users.")
                    } else call.respond(HttpStatusCode.Unauthorized, "User with id $userId not found.")
                } catch (e: EmpleadoExceptionBadRequest) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }
            }
        }
    }
}