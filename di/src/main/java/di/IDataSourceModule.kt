package di

import data.source.local.BookLocalDataSourceImpl
import data.source.local.IBookLocalDataSource
import data.source.remote.BookRemoteDataSourceImpl
import data.source.remote.IBookRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/** 데이터 소스도 상태가 없다. 저장소가 ViewModel 범위에서 하나뿐이라 여기에는 범위를 따로 붙이지 않는다. */
@Module
@InstallIn(ViewModelComponent::class)
interface IDataSourceModule {
    @Binds
    fun bindRemote(impl: BookRemoteDataSourceImpl): IBookRemoteDataSource

    @Binds
    fun bindLocal(impl: BookLocalDataSourceImpl): IBookLocalDataSource
}
