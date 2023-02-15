package rodriguez.daniel.dto

import kotlinx.serialization.Serializable

@Serializable
data class DepartamentoDTOcreacion(
    val nombre: String,
    val presupuesto: Double = 0.0
)

@Serializable
data class DepartamentoDTO(
    val nombre: String,
    val presupuesto: Double = 0.0,
    val empleados: List<EmpleadoDTO> = listOf()
)
