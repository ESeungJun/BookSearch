package core.network.di

import core.network.BuildConfig
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

/**
 * 네트워크 공통 설정. OkHttp 는 연결 풀·스레드를 클라이언트마다 따로 가지므로 앱에 하나만 둔다.
 * API 서비스(Retrofit 인터페이스)는 그것을 쓰는 data 모듈이 이 Retrofit 으로 만든다.
 */
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
        .addInterceptor(
            // BASIC 은 요청 줄과 응답 코드만 남긴다. 헤더(API 키)와 본문(401 본문에 키 일부)은 남기지 않는다
            HttpLoggingInterceptor().setLevel(
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE,
            ),
        )
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        // 응답에 이 앱이 쓰지 않는 필드(translators·status 등)가 있어 모르는 키는 무시한다.
        // explicitNulls = false: 응답에 필드가 아예 없으면 기본값 대신 null 로 받는다
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
