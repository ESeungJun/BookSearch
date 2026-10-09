package com.leeseungjun.booksearch.di

import com.leeseungjun.booksearch.data.api.RetryInterceptor
import com.leeseungjun.booksearch.data.api.searchbook.SearchBookApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://dapi.kakao.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(RetryInterceptor())
        .addInterceptor(
            // BASIC 은 요청 줄과 응답 코드만 남긴다. 헤더(API 키)와 본문(401 본문에 키 일부)은 남기지 않는다
            HttpLoggingInterceptor().setLevel(
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE,
            ),
        )
        .build()

    @Provides
    @Singleton
    fun provideSearchBookApi(client: OkHttpClient): SearchBookApi {
        // 응답에 이 앱이 쓰지 않는 필드(translators·status 등)가 있어 모르는 키는 무시한다
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SearchBookApi::class.java)
    }
}
