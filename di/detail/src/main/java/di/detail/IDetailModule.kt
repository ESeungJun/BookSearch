package di.detail

import data.detail.repo.DetailRepositoryImpl
import data.detail.source.local.DetailLocalDataSourceImpl
import data.detail.source.local.IDetailLocalDataSource
import domain.detail.repo.IDetailRepository
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
interface IDetailModule {
    @Binds
    @ViewModelScoped
    fun bindDetailRepository(impl: DetailRepositoryImpl): IDetailRepository

    @Binds
    @ViewModelScoped
    fun bindLocalDataSource(impl: DetailLocalDataSourceImpl): IDetailLocalDataSource
}
