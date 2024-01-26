package info.skyblond.vazan.domain.interceptor

import info.skyblond.vazan.domain.SettingsKey
import info.skyblond.vazan.domain.repository.ConfigRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class JimEncryptionInterceptor(
    private val configRepository: ConfigRepository,
) : Interceptor {

    private val password by lazy {
        runBlocking {
            configRepository.getConfigByKey(SettingsKey.JIM_API_PASSWORD)?.value ?: ""
        }
    }

    private fun sha256KeyGen(input: String): SecretKey {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(input.toByteArray())
        return SecretKeySpec(hash, "AES")
    }

    private val key by lazy { sha256KeyGen(password) }

    private val secureRandom = SecureRandom()

    private fun SecretKey.encrypt(input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        // random iv to initialize cipher
        val iv = ByteArray(12).also { secureRandom.nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, this, GCMParameterSpec(128, iv))
        // alloc result buffer, first 12 bytes (96bits) is IV/Nonce
        val result = ByteArray(12 + cipher.getOutputSize(input.size))
        // here we only copy 12 bytes of data
        iv.copyInto(result, 0, 0, 12)
        // directly output cipher into result buffer
        cipher.doFinal(input, 0, input.size, result, 12)
        return result
    }

    private fun SecretKey.decrypt(input: ByteArray): ByteArray? = runCatching {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE, this,
            GCMParameterSpec(128, input, 0, 12)
        )
        cipher.doFinal(input, 12, input.size - 12)
    }.getOrNull()

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        if (req.method() != "POST") return chain.proceed(req)
        val body = req.body() ?: return chain.proceed(req)
        val buffer = ByteArrayOutputStream()
        buffer.sink().buffer().use { body.writeTo(it) }
        val plain = buffer.toByteArray()
        val cipher = key.encrypt(plain)
        val newReqBody = RequestBody.create(MediaType.get("application/octet-stream"), cipher)

        val newReq = req.newBuilder().method(req.method(), newReqBody).build()
        val resp = chain.proceed(newReq)
        if (resp.code() != 200) return resp // if not 200, the resp body must be plain text
        val cipherResp = resp.body()?.bytes() ?: return resp
        val plainResp = key.decrypt(cipherResp) ?: return resp

        return resp.newBuilder().body(
            ResponseBody.create(
                MediaType.get("application/json"),
                plainResp.decodeToString()
            )
        ).build()
    }
}