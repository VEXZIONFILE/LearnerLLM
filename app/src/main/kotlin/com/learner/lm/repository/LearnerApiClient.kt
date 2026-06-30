package com.learner.lm.repository

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object LearnerApiClient {
  private var cachedService: LearnerApiService? = null

  fun createService(): LearnerApiService {
    cachedService?.let { return it }
    val baseUrl = LearnerApiConfig.baseUrl
    require(baseUrl.isNotBlank()) { "LEARNER_API_BASE_URL is not configured in local.properties" }

    val logging = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
    }
    val client = OkHttpClient.Builder()
      .addInterceptor(FirebaseAuthInterceptor())
      .addInterceptor(logging)
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(120, TimeUnit.SECONDS)
      .build()

    val service = Retrofit.Builder()
      .baseUrl(baseUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(LearnerApiService::class.java)

    cachedService = service
    return service
  }

  fun reset() {
    cachedService = null
  }
}
