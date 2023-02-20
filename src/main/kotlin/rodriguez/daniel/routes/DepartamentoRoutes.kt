package rodriguez.daniel.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import rodriguez.daniel.dto.DepartamentoDTOcreacion
import rodriguez.daniel.exception.DepartamentoExceptionBadRequest
import rodriguez.daniel.exception.DepartamentoExceptionNotFound
import rodriguez.daniel.model.Role
import rodriguez.daniel.services.departamento.DepartamentoService
import rodriguez.daniel.services.user.UserService
import java.util.*

private const val ENDPOINT = "ejercicioKtor/departamentos"

fun Application.departamentoRoutes() {
    val departamentos: DepartamentoService by inject()
    val users: UserService by inject()

    routing {
        route("/$ENDPOINT") {
            get {
                val res = departamentos.findAllDepartamentos()
                if (res.isEmpty()) call.respond(HttpStatusCode.NotFound, res)
                else call.respond(HttpStatusCode.OK, res)
            }

            get("{id}") {
                try {
                    val id = UUID.fromString(call.parameters["id"])
                    val res = departamentos.findDepartamentoById(id)
                    call.respond(HttpStatusCode.OK, res)
                } catch (e: DepartamentoExceptionNotFound) {
                    call.respond(HttpStatusCode.NotFound, e.message.toString())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }
            }

            post {
                try {
                    val dep = call.receive<DepartamentoDTOcreacion>()
                    val res = departamentos.saveDepartamento(dep)
                    call.respond(HttpStatusCode.Created, res)
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
                            val res = departamentos.deleteDepartamento(id)
                            call.respond(HttpStatusCode.OK, res)
                        } else call.respond(HttpStatusCode.Forbidden, "You can't consult all users.")
                    } else call.respond(HttpStatusCode.Unauthorized, "User with id $userId not found.")
                } catch (e: DepartamentoExceptionNotFound) {
                    call.respond(HttpStatusCode.NotFound, e.message.toString())
                } catch (e: DepartamentoExceptionBadRequest) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }
            }
        }
    }
}