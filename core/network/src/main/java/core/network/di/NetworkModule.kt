package core.network.di

import android.util.Log
import core.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
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
    private const val AUTHORIZATION = "Authorization"
    private const val LOG_TAG = "OkHttp"

    // 로그에만 쓰는 JSON 들여쓰기 출력기
    private val prettyPrinter = Json { prettyPrint = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header(AUTHORIZATION, "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(
            // debug 빌드만 요청·응답 본문까지 남기고 release 는 남기지 않는다. 인증 헤더(API 키)는 가린다.
            // 401 응답 본문에는 키 일부가 들어 있어 debug 로그캣에는 남는다(화면에는 보이지 않는다)
            HttpLoggingInterceptor(::logHttp).apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                redactHeader(AUTHORIZATION)
            },
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

    /**
     * OkHttp 로그 한 덩어리를 로그캣에 남긴다. 응답 JSON 은 한 줄로 와서 로그캣 한 줄 길이(약 4,000자)에서 잘리므로
     * 들여쓰기한 여러 줄로 펴서 줄마다 남긴다. JSON 이 아니면 그대로 남긴다.
     */
    private fun logHttp(message: String) {
        val text = if (message.startsWith("{") || message.startsWith("[")) prettyJson(message) else message
        text.lineSequence().forEach { Log.d(LOG_TAG, it) }
    }

    private fun prettyJson(raw: String): String =
        runCatching { prettyPrinter.encodeToString(JsonElement.serializer(), prettyPrinter.parseToJsonElement(raw)) }
            .getOrDefault(raw)

}
