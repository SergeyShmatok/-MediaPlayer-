package ru.netology.mediaplayer.repository

import ru.netology.mediaplayer.dto.Playlist

interface TrackRepositoryFun {

    suspend fun getPlayList(): Playlist

}


