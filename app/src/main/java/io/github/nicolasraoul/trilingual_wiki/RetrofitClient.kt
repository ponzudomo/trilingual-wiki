package io.github.nicolasraoul.trilingual_wiki

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // We provide the base URL when making the call, so a placeholder is fine here.
    private const val BASE_URL = "https://www.wikipedia.org/"

    val wikipediaApiService: WikipediaApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(WikipediaApiService::class.java)
    }
}