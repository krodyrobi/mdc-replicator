package com.example.mdcreplicator

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class KotlinDummyClass(private val client: OkHttpClient) {
    private val logger = LoggerFactory.getLogger(SampleController::class.java)


    suspend fun exampleSuspend(): String = suspendCoroutine { continuation ->
        logger.info("inside exampleSuspend")

        val request = Request.Builder().url("https://example.com").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = continuation.resumeWithException(e)

            override fun onResponse(call: Call, response: Response) {
                logger.info("inside exampleSuspend onResponse")
                continuation.resume(
                    response.body().use {
                        it?.string() ?: "N/A"
                    })
            }
        })
    }
}
