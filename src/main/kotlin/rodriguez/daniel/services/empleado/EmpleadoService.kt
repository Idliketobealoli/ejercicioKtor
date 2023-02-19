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
    suspend fun findEmpleadoById(id: UUID): EmpleadoDTO? = withContext(Dispatchers.IO) {
        eRepo.findById(id)?.toDTO()
    }

    suspend fun findAllEmpleados(): List<EmpleadoDTO> = withContext(Dispatchers.IO) {
        val empleados = eRepo.findAll().toList()
        val response = mutableListOf<EmpleadoDTO>()
        empleados.forEach { response.add(it.toDTO()) }

        response
    }

    suspend fun saveEmpleado(entity: EmpleadoDTOcreacion): EmpleadoDTO? = withContext(Dispatchers.IO) {
        dRepo.findById(entity.departamentoId) ?: return@withContext null
        eRepo.save(entity.fromDTO()).toDTO()
    }

    suspend fun deleteEmpleado(id: UUID): EmpleadoDTO? = withContext(Dispatchers.IO) {
        eRepo.delete(id)?.toDTO()
    }
}