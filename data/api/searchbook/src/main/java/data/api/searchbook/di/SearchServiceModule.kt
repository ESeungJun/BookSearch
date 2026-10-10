package data.api.searchbook.di

import data.api.searchbook.service.ISearchBookService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/** 검색 API 서비스. Retrofit(OkHttp 설정 포함)은 :core:network 가 제공한다. */
@Module
@InstallIn(SingletonComponent::class)
object SearchServiceModule {
    @Provides
    @Singleton
    fun provideSearchBookService(retrofit: Retrofit): ISearchBookService = retrofit.create(ISearchBookService::class.java)
}
