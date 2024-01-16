package info.skyblond.vazan.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.skyblond.vazan.data.ConfigRepositoryRoomImpl
import info.skyblond.vazan.data.JimRepositoryRetrofitImpl
import info.skyblond.vazan.data.LabelRepositoryRoomImpl
import info.skyblond.vazan.data.MementoRepositoryRetrofitImpl
import info.skyblond.vazan.domain.repository.ConfigRepository
import info.skyblond.vazan.domain.repository.JimRepository
import info.skyblond.vazan.domain.repository.LabelRepository
import info.skyblond.vazan.domain.repository.MementoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindConfigRepository(
        repository: ConfigRepositoryRoomImpl
    ): ConfigRepository

    @Binds
    @Singleton
    abstract fun bindLabelRepository(
        repository: LabelRepositoryRoomImpl
    ): LabelRepository

    @Binds
    @Singleton
    abstract fun bindMementoRepository(
        repository: MementoRepositoryRetrofitImpl
    ): MementoRepository

    @Binds
    @Singleton
    abstract fun bindJimRepository(
        repository: JimRepositoryRetrofitImpl
    ): JimRepository
}