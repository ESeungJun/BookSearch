package data.local.book.di

import data.local.book.repo.BookRepositoryImpl
import data.local.book.source.local.BookLocalDataSourceImpl
import data.local.book.source.local.IBookLocalDataSource
import domain.book.repo.IBookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * 저장소·데이터 소스는 상태가 없어 앱 전체에 하나일 필요가 없다. ViewModel 하나가 살아 있는 동안 같은 인스턴스를 쓴다.
 * 화면끼리 같은 데이터를 보는 것은 싱글톤 DB 의 Flow 가 맡는다.
 */
@Module
@InstallIn(ViewModelComponent::class)
interface IBookModule {
    @Binds
    @ViewModelScoped
    fun bindBookRepository(impl: BookRepositoryImpl): IBookRepository

    @Binds
    @ViewModelScoped
    fun bindLocalDataSource(impl: BookLocalDataSourceImpl): IBookLocalDataSource
}
