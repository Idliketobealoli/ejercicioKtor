package rodriguez.daniel.dto

import kotlinx.serialization.Serializable
import rodriguez.daniel.services.serializers.UUIDSerializer
import java.util.UUID

@Serializable
data class EmpleadoDTOcreacion(
    val nombre: String,
    val email: String,
    val avatar: String,
    @Serializable (with = UUIDSerializer::class)
    val departamentoId: UUID
)

@Serializable
data class EmpleadoDTO(
    val nombre: String,
    val email: String,
    val avatar: String
)
