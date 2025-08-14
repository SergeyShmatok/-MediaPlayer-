package ru.netology.mediaplayer.api

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.mediaplayer.BuildConfig
import ru.netology.mediaplayer.MediaLifecycleObserver
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Singleton
    @Provides
    fun provideLogger() = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG)
            level = HttpLoggingInterceptor.Level.BODY
    }


    @Singleton
    @Provides
    fun provideOkHttp(
        logger: HttpLoggingInterceptor,
    ) = OkHttpClient.Builder()
        .addInterceptor(logger)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    @Singleton
    @Provides
    fun provideMedia(
        mediaObserver: MediaLifecycleObserver) = mediaObserver


    @Provides
    fun provideContext (
        // Чтобы передать контекст в параметры.
        // Dagger Hilt уже всё сделал и теперь не надо забивать этим голову.- Ответственность за это
        // на библиотеке.
        @ApplicationContext
        applicationContext: Context) = applicationContext


}