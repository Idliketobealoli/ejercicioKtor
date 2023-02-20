package rodriguez.daniel.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import rodriguez.daniel.dto.*
import rodriguez.daniel.exception.UserUnauthorizedException
import rodriguez.daniel.mappers.fromDTO
import rodriguez.daniel.mappers.toDTO
import rodriguez.daniel.model.Role
import rodriguez.daniel.services.tokens.TokenService
import rodriguez.daniel.services.user.UserService
import java.util.*

private const val ENDPOINT = "ejercicioKtor/users"

fun Application.userRoutes() {

    val users: UserService by inject()
    val tokens: TokenService by inject()

    routing {
        route("/$ENDPOINT") {
            post("/register") {
                println("POST Register /$ENDPOINT/register")
                try {
                    val dto = call.receive<UserDTOcreacion>()
                    val user = users.saveUser(dto)
                    val u = users.checkEmailAndPassword(dto.email, dto.password)
                    val token = tokens.generateJWT(u)
                    call.respond(HttpStatusCode.Created, UserDTOandToken(u.toDTO(), token))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                } catch (e: RequestValidationException) {
                    call.respond(HttpStatusCode.BadRequest, e.reasons)
                }
            }

            get("/login") {
                println("POST Login /$ENDPOINT/login")
                try {
                    val dto = call.receive<UserDTOlogin>()
                    val user = users.checkEmailAndPassword(dto.email, dto.password)
                    val token = tokens.generateJWT(user)
                    call.respond(HttpStatusCode.OK, UserDTOandToken(user.toDTO(), token))
                } catch (e: UserUnauthorizedException) {
                    call.respond(HttpStatusCode.Unauthorized, e.message.toString())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                } catch (e: RequestValidationException) {
                    call.respond(HttpStatusCode.BadRequest, e.reasons)
                }
            }

            authenticate {
                get("/me") {
                    println("GET Me /$ENDPOINT/me")
                    try {
                        val jwt = call.principal<JWTPrincipal>()
                        val userId = jwt?.payload?.getClaim("id")
                            .toString().replace("\"", "")
                        val user = users.findUserById(UUID.fromString(userId.trim()))
                        if (user != null) call.respond(HttpStatusCode.OK, user)
                        else call.respond(HttpStatusCode.NotFound, "User with id $userId not found.")
                    } catch (e: UserUnauthorizedException) {
                        call.respond(HttpStatusCode.Unauthorized, e.message.toString())
                    }
                }

                get("/list") {
                    println("GET Users /$ENDPOINT/list")
                    try {
                        val jwt = call.principal<JWTPrincipal>()
                        val userId = jwt?.payload?.getClaim("id")
                            .toString().replace("\"", "")
                        val user = users.findUserById(UUID.fromString(userId.trim()))
                        if (user != null) {
                            if (user.role == Role.ADMIN) {
                                val res = users.findAllUsers()
                                if (res.isEmpty()) call.respond(HttpStatusCode.NotFound, res)
                                else call.respond(HttpStatusCode.OK, res)
                            } else call.respond(HttpStatusCode.Forbidden, "You can't consult all users.")
                        } else call.respond(HttpStatusCode.Unauthorized, "User with id $userId not found.")
                    } catch (e: UserUnauthorizedException) {
                        call.respond(HttpStatusCode.Unauthorized, e.message.toString())
                    }
                }
            }
        }
    }
}