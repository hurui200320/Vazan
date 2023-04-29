package info.skyblond.vazan.data

import android.util.Log
import info.skyblond.vazan.data.retrofit.EntryDto
import info.skyblond.vazan.data.retrofit.MementoService
import info.skyblond.vazan.data.retrofit.UpdateEntryDto
import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.model.LibraryBrief
import info.skyblond.vazan.domain.model.LibraryField
import info.skyblond.vazan.domain.repository.ConfigRepository
import info.skyblond.vazan.domain.repository.MementoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MementoRepositoryRetrofitImpl @Inject constructor(
    private val service: MementoService,
    private val config: ConfigRepository
) : MementoRepository {
    private val tag = "MementoRepositoryRetrofitImpl"

    private suspend fun getToken(): String =
        config.getConfigByKey(SettingsKey.MEMENTO_API_KEY)?.value ?: ""

    override suspend fun listLibraries(): List<LibraryBrief> =
        service.listLibraries(getToken()).libraries.map { LibraryBrief.fromDto(it) }

    override suspend fun getLibraryFields(libraryId: String): List<LibraryField> =
        service.getLibraryInfo(libraryId, getToken()).fields.map { LibraryField.fromDto(it) }

    override suspend fun listEntriesByLibraryId(
        libraryId: String,
        pageSize: Int?,
        fields: String?,
        startRevision: Int?,
    ): Flow<List<EntryDto>> {
        val token = getToken()

        return flow {
            var pageToken: String? = null
            do {
                val r = try {
                    service.listEntriesByLibraryId(
                        libraryId = libraryId,
                        token = token,
                        pageSize = pageSize,
                        pageToken = pageToken,
                        fields = fields,
                        startRevision = startRevision
                    )
                } catch (e: IOException) {
                    Log.e(tag, "Error when fetching entries: $libraryId, $pageToken", e)
                    delay(5000)
                    continue
                } catch (e: HttpException) {
                    Log.e(tag, "Error when fetching entries: $libraryId, $pageToken", e)
                    delay(5000)
                    continue
                }
                val t = System.currentTimeMillis()
                pageToken = r.nextPageToken
                emit(r.entries)
                val dt = System.currentTimeMillis() - t
                // the api has rate limit of 30 request per minute
                // thus wait 2s for each request
                if (dt < 3000) {
                    delay(3000 - dt)
                }
            } while (pageToken != null)
        }
    }

    override suspend fun updateEntryByLibraryIdAndEntryId(
        libraryId: String,
        entryId: String,
        updateEntryDto: UpdateEntryDto
    ): EntryDto = service.updateEntryByLibraryIdAndEntryId(
        libraryId = libraryId,
        entryId = entryId,
        updateEntryDto = updateEntryDto,
        token = getToken()
    )
}