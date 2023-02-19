package rodriguez.daniel.dto

import kotlinx.serialization.Serializable
import rodriguez.daniel.model.Role

@Serializable
data class UserDTOcreacion(
    val email: String,
    val password: String,
    val role: Role = Role.EMPLEADO
)

@Serializable
data class UserDTOlogin(
    val email: String,
    val password: String
)

@Serializable
data class UserDTO(
    val email: String,
    val role: Role = Role.EMPLEADO
)

@Serializable
data class UserDTOandToken(
    val user: UserDTO,
    val token: String
)