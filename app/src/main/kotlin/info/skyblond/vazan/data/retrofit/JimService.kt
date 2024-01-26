package info.skyblond.vazan.data.retrofit

import retrofit2.http.Body
import retrofit2.http.POST

interface JimService {
    @POST("/")
    suspend fun sendStringListResp(@Body req: JimRequestDto): JimStringListResponseDto
    @POST("/")
    suspend fun sendEntryResp(@Body req: JimRequestDto): JimEntryResponseDto
    @POST("/")
    suspend fun sendMetaResp(@Body req: JimRequestDto): JimMetaResponseDto

}