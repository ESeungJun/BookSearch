package di

import data.source.local.BookLocalDataSourceImpl
import data.source.local.IBookLocalDataSource
import data.source.remote.BookRemoteDataSourceImpl
import data.source.remote.IBookRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * 로컬 데이터 소스는 저장소 셋(책·검색·즐겨찾기)이 함께 쓴다. 범위가 없으면 저장소마다 새로 만들어지므로
 * ViewModel 범위로 묶어 한 ViewModel 안에서는 같은 인스턴스를 쓴다. 앱 전체에 하나일 이유(상태·비싼 생성)는 없다.
 */
@Module
@InstallIn(ViewModelComponent::class)
interface IDataSourceModule {
    @Binds
    @ViewModelScoped
    fun bindRemote(impl: BookRemoteDataSourceImpl): IBookRemoteDataSource

    @Binds
    @ViewModelScoped
    fun bindLocal(impl: BookLocalDataSourceImpl): IBookLocalDataSource
}
