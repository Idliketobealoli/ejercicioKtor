package rodriguez.daniel.services.departamento

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
class DepartamentoService(
    @Named("EmpleadoRepositoryCached")
    private val eRepo: IEmpleadoRepository,
    @Named("DepartamentoRepositoryCached")
    private val dRepo: IDepartamentoRepository,
) {
    suspend fun findDepartamentoById(id: UUID): DepartamentoDTO? = withContext(Dispatchers.IO) {
        dRepo.findById(id)?.toDTO()
    }

    suspend fun findAllDepartamentos(): List<DepartamentoDTO> = withContext(Dispatchers.IO) {
        val empleados = dRepo.findAll().toList()
        val response = mutableListOf<DepartamentoDTO>()
        empleados.forEach { response.add(it.toDTO()) }

        response
    }

    suspend fun saveDepartamento(entity: DepartamentoDTOcreacion): DepartamentoDTO = withContext(Dispatchers.IO) {
        dRepo.save(entity.fromDTO()).toDTO()
    }

    suspend fun deleteDepartamento(id: UUID): DepartamentoDTO? = withContext(Dispatchers.IO) {
        val entity = dRepo.findById(id) ?: return@withContext null
        if (entity.toDTO().empleados.isNotEmpty()) return@withContext null

        dRepo.delete(id)?.toDTO()
    }
}