package ru.netology.mediaplayer.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.mediaplayer.repository.TrackRepository
import ru.netology.mediaplayer.repository.TrackRepositoryFun
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindsTrackRepository(repository: TrackRepository): TrackRepositoryFun




}





