package di

import data.repo.BookRepositoryImpl
import domain.repo.IBookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * 저장소는 상태를 갖지 않아 앱 전체에 하나일 필요가 없다. ViewModel 하나가 살아 있는 동안만 같은 인스턴스를 쓴다.
 * 화면끼리 같은 데이터를 보는 것은 싱글톤 DB 의 Flow 가 맡는다.
 */
@Module
@InstallIn(ViewModelComponent::class)
interface IRepositoryModule {
    @Binds
    @ViewModelScoped
    fun bindBookRepository(impl: BookRepositoryImpl): IBookRepository
}
