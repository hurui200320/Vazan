package info.skyblond.vazan.domain.interceptor

import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.repository.ConfigRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class JimHostSelectionInterceptor(
    private val configRepository: ConfigRepository,
) : Interceptor {

    private val host by lazy {
        runBlocking {
            configRepository.getConfigByKey(SettingsKey.JIM_HOST)?.value ?: ""
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val newReq = req.newBuilder().url(host).build()
        return chain.proceed(newReq)
    }
}