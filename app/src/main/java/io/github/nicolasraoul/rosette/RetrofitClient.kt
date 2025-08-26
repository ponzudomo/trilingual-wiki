package io.github.nicolasraoul.rosette

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // We provide the base URL when making the call, so a placeholder is fine here.
    private const val BASE_URL = "https://www.wikipedia.org/"

    val wikipediaApiService: WikipediaApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("API_REQUEST", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val userAgentInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", "Rosette/1.8 (https://github.com/nicolas-raoul/trilingual-wiki; nicolas.raoul@gmail.com)")
                .build()
            chain.proceed(requestWithUserAgent)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(WikipediaApiService::class.java)
    }
}