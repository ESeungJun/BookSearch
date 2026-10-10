package di.search

import data.search.service.ISearchBookService
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

/** OkHttp 는 연결 풀·스레드를 클라이언트마다 따로 가지므로 앱에 하나만 둔다. Retrofit 서비스도 그 클라이언트에 묶여 하나다. */
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
    fun provideSearchBookService(client: OkHttpClient): ISearchBookService {
        // 응답에 이 앱이 쓰지 않는 필드(translators·status 등)가 있어 모르는 키는 무시한다.
        // explicitNulls = false: 응답에 필드가 아예 없으면 기본값 대신 null 로 받는다(D-60)
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ISearchBookService::class.java)
    }
}
