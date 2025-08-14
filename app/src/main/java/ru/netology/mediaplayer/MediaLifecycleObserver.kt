package ru.netology.mediaplayer

import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject


class MediaLifecycleObserver @Inject constructor(
) : LifecycleEventObserver {   // Для подписки на жизненный цикл

    var playbackPosition = 0

    var pauseOn = false

    var mediaPlayer: MediaPlayer? = MediaPlayer()

    fun play(url: String) {

        mediaPlayer?.setDataSource(url)

        mediaPlayer?.isLooping = false
        pauseOn = false

        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener { it ->
//            mediaPlayer?.seekTo(450000)
            it.start()
        }

    }

    fun playAfterPause() {
        mediaPlayer?.seekTo(playbackPosition)
        mediaPlayer?.start()
        pauseOn = false
    }


    fun pause() {
        mediaPlayer?.pause()
        playbackPosition = mediaPlayer?.currentPosition ?: 0
        pauseOn = true
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        // В него приходит источник👆 жизненного цикла (Fragment или Activity)
        // И event жизненного цикла

        when (event) { // Обработаем события жизненного цикла:

            Lifecycle.Event.ON_PAUSE -> mediaPlayer?.pause()
            Lifecycle.Event.ON_STOP -> {
                mediaPlayer?.release()
                mediaPlayer = null
            }

            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            // Параметры: observer — Наблюдатель, который необходимо удалить👆.
            else -> Unit

            // Потребуются: ON_PAUSE, ON_STOP, ON_DESTROY. Оставим только их.

        }


    }

}