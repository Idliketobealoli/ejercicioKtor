package rodriguez.daniel.mappers

import rodriguez.daniel.dto.EmpleadoDTO
import rodriguez.daniel.dto.EmpleadoDTOcreacion
import rodriguez.daniel.model.Empleado

fun Empleado.toDTO() = EmpleadoDTO(nombre, email, avatar)
fun EmpleadoDTOcreacion.fromDTO() = Empleado(
    nombre = nombre, email = email,
    avatar = avatar, departamentoId = departamentoId)