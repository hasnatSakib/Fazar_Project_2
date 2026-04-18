package com.example.fazarproject2.di

import android.content.Context
import androidx.room.Room
import com.example.fazarproject2.data.local.daos.AlarmDao
import com.example.fazarproject2.data.local.AlarmDatabase
import com.example.fazarproject2.data.local.daos.AudioDao
import com.example.fazarproject2.data.remote.SunriseApi
import com.example.fazarproject2.data.repositoryimpl.AlarmRepositoryImpl
import com.example.fazarproject2.data.repositoryimpl.MediaRepositoryImpl
import com.example.fazarproject2.data.repositoryimpl.SunriseRepositoryImpl
import com.example.fazarproject2.domain.repository.AlarmRepository
import com.example.fazarproject2.domain.repository.MediaRepository
import com.example.fazarproject2.domain.repository.SunriseRepository
import com.example.fazarproject2.util.MediaScanner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideSunriseApi(): SunriseApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(SunriseApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(SunriseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAlarmDatabase(@ApplicationContext context: Context): AlarmDatabase {
        return Room.databaseBuilder(
            context,
            AlarmDatabase::class.java,
            AlarmDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development simplicity with schema changes
            .build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(db: AlarmDatabase): AlarmDao = db.alarmDao

    @Provides
    @Singleton
    fun provideAudioDao(db: AlarmDatabase): AudioDao = db.audioDao

    @Provides
    @Singleton
    fun provideMediaScanner(@ApplicationContext context: Context): MediaScanner {
        return MediaScanner(context)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(
        mediaScanner: MediaScanner,
        audioDao: AudioDao
    ): MediaRepository {
        return MediaRepositoryImpl(mediaScanner, audioDao)
    }

    @Provides
    @Singleton
    fun provideSunriseRepository(api: SunriseApi): SunriseRepository {
        return SunriseRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideAlarmRepository(
        dao: AlarmDao,
        @ApplicationContext context: Context
    ): AlarmRepository {
        return AlarmRepositoryImpl(dao, context)
    }
}
