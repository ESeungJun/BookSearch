package data.api.searchbook.di

import data.api.searchbook.service.ISearchBookService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit

/** 검색 API 서비스. Retrofit(OkHttp 설정 포함)은 :core:network 가 제공한다. 저장소·데이터 소스와 같이 ViewModel 범위에서 만든다. */
@Module
@InstallIn(ViewModelComponent::class)
object SearchServiceModule {
    @Provides
    fun provideSearchBookService(retrofit: Retrofit): ISearchBookService = retrofit.create(ISearchBookService::class.java)
}
