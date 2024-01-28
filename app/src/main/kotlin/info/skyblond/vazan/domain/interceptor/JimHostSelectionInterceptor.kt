package info.skyblond.vazan.domain.interceptor

import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.repository.ConfigRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class JimHostSelectionInterceptor(
    configRepository: ConfigRepository,
) : Interceptor {

    private val host by RefreshableConfig(configRepository, SettingsKey.JIM_HOST)

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val newReq = req.newBuilder().url(host).build()
        return chain.proceed(newReq)
    }
}