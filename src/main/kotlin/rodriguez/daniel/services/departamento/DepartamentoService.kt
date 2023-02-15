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
    suspend fun findDepartamentoById(id: UUID): Response<out DepartamentoDTO> = withContext(Dispatchers.IO) {
        val entity = dRepo.findById(id)

        if (entity == null) ResponseError(404, "Departamento with id $id not found.")
        else ResponseSuccess(200, entity.toDTO())
    }

    suspend fun findAllDepartamentos(): Response<out List<DepartamentoDTO>> = withContext(Dispatchers.IO) {
        val empleados = dRepo.findAll().toList()
        val response = mutableListOf<DepartamentoDTO>()
        empleados.forEach { response.add(it.toDTO()) }

        if (response.isEmpty()) ResponseError(404, "There are no departamentos.")
        else ResponseSuccess(200, response)
    }

    suspend fun saveDepartamento(entity: DepartamentoDTOcreacion): Response<DepartamentoDTO> = withContext(Dispatchers.IO) {
        ResponseSuccess(201, dRepo.save(entity.fromDTO()).toDTO())
    }

    suspend fun deleteDepartamento(id: UUID): Response<out DepartamentoDTO> = withContext(Dispatchers.IO) {
        val entity = dRepo.findById(id) ?: return@withContext ResponseError(404, "Departamento with id $id not found.")
        if (entity.toDTO().empleados.isNotEmpty())
            return@withContext ResponseError(400, "Cannot delete a departamento with assigned empleados.")

        val res = dRepo.delete(id)

        if (res == null) ResponseError(404, "Departamento with id $id not found.")
        else ResponseSuccess(200, res.toDTO())
    }
}