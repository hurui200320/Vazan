package info.skyblond.vazan.data.retrofit

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface MementoService {
    @GET("v1/libraries")
    suspend fun listLibraries(@Query("token") token: String): ListLibrariesDto

    @GET("v1/libraries/{libraryId}")
    suspend fun getLibraryInfo(
        @Path("libraryId") libraryId: String,
        @Query("token") token: String
    ): GetLibraryDto

    /**
     * The [fields] is fields id list.
     *
     * For example, if only want to list values for id 1 and 2,
     * this field should be "1,2"
     * */
    @GET("v1/libraries/{libraryId}/entries")
    suspend fun listEntriesByLibraryId(
        @Path("libraryId") libraryId: String,
        @Query("token") token: String,
        @Query("pageSize") pageSize: Int? = null,
        @Query("pageToken") pageToken: String? = null,
        @Query("fields") fields: String? = null,
        @Query("startRevision") startRevision: Int? = null,
    ): ListEntriesByLibraryIdDto

    @PATCH("v1/libraries/{libraryId}/entries/{entryId}")
    suspend fun updateEntryByLibraryIdAndEntryId(
        @Path("libraryId") libraryId: String,
        @Path("entryId") entryId: String,
        @Body updateEntryDto: UpdateEntryDto,
        @Query("token") token: String
    ): EntryDto
}