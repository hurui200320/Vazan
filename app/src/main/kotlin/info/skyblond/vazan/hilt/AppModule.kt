package info.skyblond.vazan.hilt

import android.app.Application
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.skyblond.vazan.data.retrofit.MementoService
import info.skyblond.vazan.data.room.AppDatabase
import info.skyblond.vazan.data.room.ConfigDao
import info.skyblond.vazan.data.room.LabelDao
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
    fun provideLabelDao(database: AppDatabase): LabelDao = database.labelDao

    @Provides
    @Singleton
    fun provideKotlinMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideMementoService(moshi: Moshi): MementoService = Retrofit.Builder()
        .baseUrl("https://api.mementodatabase.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build().create()
}