package ru.netology.mediaplayer.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.netology.mediaplayer.repository.TrackRepository
import javax.inject.Inject


@HiltViewModel
class TrackViewModel @Inject constructor(
    private val repository: TrackRepository,
) : ViewModel() {

    suspend fun getPlaylist() = repository.getPlayList()

    val dataState = repository.dataState

}
