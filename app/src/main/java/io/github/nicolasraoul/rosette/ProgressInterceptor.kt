package io.github.nicolasraoul.rosette

import android.os.Handler
import android.os.Looper
import okhttp3.*
import okio.*
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

// Interface for progress updates
interface OnProgressListener {
    fun onProgress(progress: Int)
}

object ProgressInterceptor : Interceptor {

    // Use a thread-safe map to store listeners for each URL
    private val listeners = ConcurrentHashMap<String, OnProgressListener>()

    fun addListener(url: String, listener: OnProgressListener) {
        listeners[url] = listener
    }

    fun removeListener(url: String) {
        listeners.remove(url)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)
        val url = originalRequest.url.toString()

        val listener = listeners[url]
        return if (listener != null) {
            response.newBuilder()
                .body(ProgressResponseBody(response.body!!, listener))
                .build()
        } else {
            response
        }
    }

    private class ProgressResponseBody(
        private val responseBody: ResponseBody,
        private val progressListener: OnProgressListener
    ) : ResponseBody() {

        private var bufferedSource: BufferedSource? = null

        override fun contentType(): MediaType? = responseBody.contentType()

        override fun contentLength(): Long = responseBody.contentLength()

        override fun source(): BufferedSource {
            if (bufferedSource == null) {
                bufferedSource = source(responseBody.source()).buffer()
            }
            return bufferedSource!!
        }

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead = 0L
                val contentLength = responseBody.contentLength()
                val handler = Handler(Looper.getMainLooper())

                @Throws(IOException::class)
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                    val progress = if (contentLength > 0) {
                        (100L * totalBytesRead / contentLength).toInt()
                    } else {
                        // If we don't know content length, use a simple increasing progress
                        // This handles cases where server doesn't send Content-Length header
                        minOf(90, (totalBytesRead / 1024).toInt()) // 1KB = 1%, max 90%
                    }

                    // Update progress on main thread
                    handler.post {
                        progressListener.onProgress(progress)
                    }

                    return bytesRead
                }
            }
        }
    }
}