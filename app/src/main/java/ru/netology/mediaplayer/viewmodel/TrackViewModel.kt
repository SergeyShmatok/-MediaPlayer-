package ru.netology.mediaplayer.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.mediaplayer.dto.Track
import ru.netology.mediaplayer.repository.TrackRepository
import javax.inject.Inject


@HiltViewModel
class TrackViewModel @Inject constructor(
    private val repository: TrackRepository,
) : ViewModel() {

    suspend fun getPlaylist() = repository.getPlayList()

    val dataState = repository.dataState

    private var _currentTrack = MutableStateFlow(Track(id = 0, file = ""))
    val currentTrack: StateFlow<Track> = _currentTrack.asStateFlow()

    private var _previousTrack = MutableStateFlow(Track(id = 0, file = ""))
    val previousTrack: StateFlow<Track> = _previousTrack.asStateFlow()

    private var _isClicked = MutableStateFlow(false)
    val isClicked: StateFlow<Boolean> = _isClicked.asStateFlow()


    fun setTrack(track: Track) {
        _currentTrack.value = track
    }

    fun setPreviousTrack(track: Track) {
        _previousTrack.value = track
    }

    fun setClicked(isClicked: Boolean) {
        _isClicked.value = isClicked
    }

}
