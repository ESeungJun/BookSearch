package di.network

import data.search.service.ISearchBookService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/** API 서비스(Retrofit 인터페이스) 모음. 새 API 가 생기면 여기에 provide 하나를 더한다. */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideSearchBookService(retrofit: Retrofit): ISearchBookService = retrofit.create(ISearchBookService::class.java)
}
