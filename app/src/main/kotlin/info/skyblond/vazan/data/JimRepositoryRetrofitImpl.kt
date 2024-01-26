package info.skyblond.vazan.data

import android.util.Log
import info.skyblond.vazan.data.retrofit.EntryDto
import info.skyblond.vazan.data.retrofit.JimRequestDto
import info.skyblond.vazan.data.retrofit.JimService
import info.skyblond.vazan.data.retrofit.MementoService
import info.skyblond.vazan.data.retrofit.UpdateEntryDto
import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.model.JimEntry
import info.skyblond.vazan.domain.model.JimEntry.Companion.toModel
import info.skyblond.vazan.domain.model.JimMeta
import info.skyblond.vazan.domain.model.JimMeta.Companion.toModel
import info.skyblond.vazan.domain.model.LibraryBrief
import info.skyblond.vazan.domain.model.LibraryField
import info.skyblond.vazan.domain.repository.ConfigRepository
import info.skyblond.vazan.domain.repository.JimRepository
import info.skyblond.vazan.domain.repository.MementoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class JimRepositoryRetrofitImpl @Inject constructor(
    private val service: JimService,
) : JimRepository {
    private val tag = "JimRepositoryRetrofitImpl"

    private fun jimReq(command: String, params: List<*>) = JimRequestDto(
        UUID.randomUUID().toString(),
        System.currentTimeMillis() / 1000,
        command, params
    )

    private suspend fun <T> safeDoing(default: T, block: suspend () -> T): T {
        return try {
            block()
        } catch (t: Throwable) {
            Log.e(tag, "Error when requesting jim server", t)
            default
        }
    }

    override suspend fun browse(parentId: String?): List<String> = safeDoing(emptyList()) {
        service.sendStringListResp(
            jimReq("browse", listOf(parentId))
        ).let {
            if (it.err != null) {
                throw RuntimeException("Failed browsing with parent id `${parentId}`: ${it.err}")
            } else it.result
        }
    }

    override suspend fun search(keywords: List<String>): List<String> = safeDoing(emptyList()) {
        service.sendStringListResp(
            jimReq("search", keywords.filter { it.isNotBlank() })
        ).let {
            if (it.err != null) {
                throw RuntimeException("Failed search with keywords `${keywords}`: ${it.err}")
            } else it.result
        }
    }

    override suspend fun view(entryId: String): JimEntry? = safeDoing(null) {
        service.sendEntryResp(
            jimReq("view", listOf(entryId))
        ).let {
            if (it.err != null) {
                throw RuntimeException("Failed viewing `${entryId}`: ${it.err}")
            } else it.result.toModel()
        }
    }

    override suspend fun createEntry(
        entryId: String,
        type: String?,
        parentId: String?,
        name: String,
        note: String
    ): JimEntry? = safeDoing(null) {
        val params = listOf(entryId, type, parentId, name, note)
        service.sendEntryResp(
            jimReq("create_entry", params)
        ).let {
            if (it.err != null) {
                throw RuntimeException("Failed creating entry `${params}`: ${it.err}")
            } else it.result.toModel()
        }
    }

    override suspend fun updateEntry(
        entryId: String,
        fieldValuePairs: List<Pair<String, String?>>
    ): JimEntry? = safeDoing(null) {
        val params = listOf(entryId, *fieldValuePairs.flatMap { listOf(it.first, it.second) }.toTypedArray())
        service.sendEntryResp(
            jimReq("update_entry", params)
        ).let {
            if (it.err != null) {
                throw RuntimeException("Failed updating entry `${params}`: ${it.err}")
            } else it.result.toModel()
        }
    }

    override suspend fun deleteEntry(entryId: String): JimEntry? = safeDoing(null) {
        service.sendEntryResp(
            jimReq("delete_entry", listOf(entryId))
        ).let {
            if (it.err != null) {
                throw RuntimeException("Failed deleting entry `${entryId}`: ${it.err}")
            } else it.result.toModel()
        }
    }

    override suspend fun createMeta(
        entryId: String,
        name: String,
        type: String,
        value: String
    ): JimEntry? = safeDoing(null) {
        val params = listOf(entryId, name, type, value)
        service.sendEntryResp(
            jimReq("create_meta", params)
        ).let {
            if (it.err != null) {
                throw RuntimeException("Failed creating meta `${params}`: ${it.err}")
            } else it.result.toModel()
        }
    }

    override suspend fun updateMeta(
        entryId: String, name: String,
        fieldValuePairs: List<Pair<String, String?>>
    ): JimMeta? = safeDoing(null) {
        val params = listOf(entryId, name, *fieldValuePairs.flatMap { listOf(it.first, it.second) }.toTypedArray())
        service.sendMetaResp(
            jimReq("update_meta", params)
        ).let {
            if (it.err != null) {
                throw RuntimeException("Failed updating meta `${params}`: ${it.err}")
            } else it.result.toModel()
        }
    }

    override suspend fun deleteMeta(entryId: String, name: String): JimMeta? = safeDoing(null) {
        service.sendMetaResp(
            jimReq("delete_meta", listOf(entryId, name))
        ).let {
            if (it.err != null) {
                throw RuntimeException("Failed deleting meta `${entryId}`: ${it.err}")
            } else it.result.toModel()
        }
    }
}