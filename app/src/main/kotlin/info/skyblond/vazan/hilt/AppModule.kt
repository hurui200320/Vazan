package info.skyblond.vazan.hilt

import android.app.Application
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.skyblond.vazan.data.retrofit.JimService
import info.skyblond.vazan.data.room.AppDatabase
import info.skyblond.vazan.data.room.ConfigDao
import info.skyblond.vazan.domain.interceptor.JimEncryptionInterceptor
import info.skyblond.vazan.domain.interceptor.JimHostSelectionInterceptor
import info.skyblond.vazan.domain.repository.ConfigRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "vazan").build()

    @Provides
    @Singleton
    fun provideConfigDao(database: AppDatabase): ConfigDao = database.configDao

    @Provides
    @Singleton
    fun provideKotlinMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideJimService(moshi: Moshi, configRepository: ConfigRepository): JimService =
        Retrofit.Builder()
            .baseUrl("http://example.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(JimHostSelectionInterceptor(configRepository))
                    .addInterceptor(JimEncryptionInterceptor(configRepository))
                    .build()
            )
            .build().create()
}