package rodriguez.daniel.services.empleado

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import rodriguez.daniel.dto.*
import rodriguez.daniel.mappers.fromDTO
import rodriguez.daniel.mappers.toDTO
import rodriguez.daniel.repositories.departamento.IDepartamentoRepository
import rodriguez.daniel.repositories.empleado.IEmpleadoRepository
import java.util.UUID

@Single
class EmpleadoService(
    @Named("EmpleadoRepositoryCached")
    private val eRepo: IEmpleadoRepository,
    @Named("DepartamentoRepositoryCached")
    private val dRepo: IDepartamentoRepository,
) {
    suspend fun findEmpleadoById(id: UUID): Response<out EmpleadoDTO> = withContext(Dispatchers.IO) {
        val entity = eRepo.findById(id)

        if (entity == null) ResponseError(404, "Empleado with id $id not found.")
        else ResponseSuccess(200, entity.toDTO())
    }

    suspend fun findAllEmpleados(): Response<out List<EmpleadoDTO>> = withContext(Dispatchers.IO) {
        val empleados = eRepo.findAll().toList()
        val response = mutableListOf<EmpleadoDTO>()
        empleados.forEach { response.add(it.toDTO()) }

        if (response.isEmpty()) ResponseError(404, "There are no empleados.")
        else ResponseSuccess(200, response)
    }

    suspend fun saveEmpleado(entity: EmpleadoDTOcreacion): Response<out EmpleadoDTO> = withContext(Dispatchers.IO) {
        dRepo.findById(entity.departamentoId) ?: return@withContext ResponseError(
            400, "Cannot insert an empleado with a department id from a non-existent department."
        )
        ResponseSuccess(201, eRepo.save(entity.fromDTO()).toDTO())
    }

    suspend fun deleteEmpleado(id: UUID): Response<out EmpleadoDTO> = withContext(Dispatchers.IO) {
        val empleado = eRepo.delete(id)

        if (empleado == null) ResponseError(404, "Empleado with id $id not found.")
        else ResponseSuccess(200, empleado.toDTO())
    }
}