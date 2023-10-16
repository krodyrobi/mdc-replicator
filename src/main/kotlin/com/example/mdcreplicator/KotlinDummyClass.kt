package com.example.mdcreplicator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class KotlinDummyClass(
    private val client: OkHttpClient,
    restTemplateBuilder: RestTemplateBuilder,
    webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(SampleController::class.java)
    private val restTemplate = restTemplateBuilder.rootUri(URL).build()
    private val webClient = webClientBuilder.baseUrl(URL).build()


    suspend fun okHttpSuspend(): String = suspendCoroutine { continuation ->
        logger.info("inside okHttpSuspend")

        val request = Request.Builder().url("$URL/monoOkHttp").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = continuation.resumeWithException(e)

            override fun onResponse(call: Call, response: Response) {
                logger.info("inside enqueue onResponse")
                continuation.resume(
                    response.body().use {
                        it?.string() ?: "N/A"
                    })
            }
        })
    }

    suspend fun blockingOkHttpSuspend(): String = suspendCoroutine { continuation ->
        logger.info("inside blockingOkHttpSuspend")

        val request = Request.Builder().url("$URL/monoBlockingOkHttp").build()
        try {
            continuation.resume(client.newCall(request).execute().body().use { it?.string() ?: "N/A" })
        } catch (e: Throwable) {
            continuation.resumeWithException(e);
        }
    }

    suspend fun restTemplateSuspend(): String =
        withContext(Dispatchers.IO) {
            logger.info("within withContext")
            restTemplate.getForObject("/monoRestTemplate", String::class.java) ?: "N/A"
        }

    suspend fun webClientSuspend(): String =
        withContext(Dispatchers.IO) {
            logger.info("within withContext")
            webClient.get().uri("/monoWebClient").retrieve().awaitBody<String>()
        }

    companion object {
        const val URL: String = "https://eny7z0f6m5g2.x.pipedream.net"
    }
}
