package ru.netology.mediaplayer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.netology.mediaplayer.BuildConfig.BASE_URL
import ru.netology.mediaplayer.adapter.OnInteractionListener
import ru.netology.mediaplayer.adapter.TrackAdapter
import ru.netology.mediaplayer.databinding.ActivityAppBinding
import ru.netology.mediaplayer.databinding.SongCardBinding
import ru.netology.mediaplayer.dto.Track
import ru.netology.mediaplayer.viewmodel.TrackViewModel


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val observer = MediaLifecycleObserver()

    private val trackViewModel by viewModels<TrackViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val appBinding = ActivityAppBinding.inflate(layoutInflater)

        setContentView(appBinding.root)

        lifecycle.addObserver(observer) // Подписка на жизненный цикл

        var playingWhilePlaying = false

        val adapter = TrackAdapter(object : OnInteractionListener {
            //                      Если включить этот👇 код у треков появиться правильное время
//
//                                                override fun getMetaData(url: String): String {
//
//                var durations = ""
//
//                lifecycleScope.launch {
//
//                    val retriever = MediaMetadataRetriever()
//                    retriever.setDataSource(url)
//                    val time =
//                        (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))?.toFloat()
//                            ?: 0
//                    durations = ("%.2f".format(time.toFloat() / 60000)).replace(".", ":")
//                }
//
//                return durations
//            }
            override fun getPosition(): Int {
                return trackViewModel.currentTrack.value.id - 1
            }

            override fun getPreviousPosition(): Int {
                return trackViewModel.previousTrack.value.id - 1
            }

            override fun isClicked(): Boolean {
                return trackViewModel.isClicked.value
            }

            override fun onClick(
                track: Track,
                binding: SongCardBinding,
            ) {

                val url = "${BASE_URL}${track.file}"

                trackViewModel.apply {
                    setPreviousTrack(currentTrack.value)
                    setTrack(track)
                }

                fun settingIconsParameters() {

                    binding.apply {

                        iconPlay.isChecked = true
                        appBinding.fab.isChecked = true

                        iconLike.apply {
                            visibility = View.VISIBLE
                            setOnClickListener {
                                val text =
                                    if (this.isChecked) "Трек добавлен в избранное" else "Удалён из избранного"
                                Toast.makeText(context, text, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }

                        iconNot.visibility = View.VISIBLE
                    }

                }


                val currentPosition = trackViewModel.currentTrack.value.id - 1

                println("Текущая позиция: $currentPosition")

                val previousPosition = trackViewModel.previousTrack.value.id - 1

                println("Предыдущая позиция: $previousPosition")

                when (observer.mediaPlayer?.isPlaying) {

                    false -> {

                        settingIconsParameters()

                        if (!observer.pauseOn) {
                            observer.play(url)
                            trackViewModel.setClicked(true)
                        }

                        if (observer.pauseOn && currentPosition != previousPosition) {

                            trackViewModel.setClicked(false)

                            observer.mediaPlayer?.stop()
                            observer.mediaPlayer?.reset()

                            observer.play(url)
                            trackViewModel.setClicked(true)
                        }

                        if (observer.pauseOn && currentPosition == previousPosition) {
                            observer.playAfterPause()
                            trackViewModel.setClicked(true)
                        }


                    }

                    true -> {

                        if (currentPosition == previousPosition) {

                            binding.apply {

                                iconPlay.isChecked = false
                                appBinding.fab.isChecked = false

                                iconLike.visibility = View.INVISIBLE
                                iconNot.visibility = View.INVISIBLE

                                observer.pause()
                                trackViewModel.setClicked(false)

                            }

                        }

                        if (currentPosition != previousPosition) {

                            playingWhilePlaying = true
                            trackViewModel.setClicked(false)

                            observer.mediaPlayer?.stop()
                            observer.mediaPlayer?.reset()

                            settingIconsParameters()

                            observer.play(url)

                            lifecycle.coroutineScope.launch {

                                playingWhilePlaying = false
                                delay(50)
                                trackViewModel.setClicked(true)

                            }

                        }

                    }

                    else -> {}
                }
            }

        })

        appBinding.list.adapter = adapter

        appBinding.list.itemAnimator = null

        trackViewModel.dataState.flowWithLifecycle(lifecycle).onEach { stateModel ->

            appBinding.apply {

                progress.isVisible = stateModel.loading
                albumTitle.isVisible = !stateModel.loading
                albumName.isVisible = !stateModel.loading
                singerTitle.isVisible = !stateModel.loading
                singerName.isVisible = !stateModel.loading
                genreAndYear.isVisible = !stateModel.loading
                fab.isVisible = !stateModel.loading
                listBox.isVisible = !stateModel.loading

            }


        }.launchIn(lifecycleScope)

        lifecycleScope.launch {

            val playlist = trackViewModel.getPlaylist()
            appBinding.albumName.text = playlist.title
            appBinding.singerName.text = playlist.artist
            val genreAndYear = "${playlist.published}, ${playlist.genre}"
            appBinding.genreAndYear.text = genreAndYear
            adapter.submitList(playlist.tracks)

        }


        observer.mediaPlayer?.setOnCompletionListener {

            val currentPosition = trackViewModel.currentTrack.value.id - 1

            val listSize = adapter.currentList.size
            fun scrollTo(num: Int) = appBinding.list.scrollToPosition(num)

            trackViewModel.setClicked(false)

            it.stop()
            it.reset()

            val position = if (currentPosition + 1 <= listSize - 1) currentPosition + 1 else 0

            lifecycleScope.launch {

                scrollTo(currentPosition)
                delay(50)
                trackViewModel.setClicked(false)
                delay(50)
                if (currentPosition < listSize - 1) scrollTo(currentPosition + 1)
                else scrollTo(0)
                delay(50)

                appBinding.list.findViewHolderForAdapterPosition(position)
                    ?.itemView?.findViewById<MaterialButton>(R.id.icon_play)?.performClick()
                trackViewModel.setClicked(true)

            }

        }

        appBinding.apply {

            fab.setOnClickListener { it ->

                when (observer.mediaPlayer?.isPlaying) {

                    false -> {

                        if (observer.pauseOn) {
                            fab.isChecked = true

                            trackViewModel.setClicked(true)

                            observer.playAfterPause()
                            trackViewModel.setClicked(true)
                        } else {
                            fab.isChecked = false

                        }

                    }

                    true -> {
                        fab.isChecked = false

                        trackViewModel.setClicked(false)

                        observer.pause()

                    }

                    else -> {}
                }

            }
        }

        trackViewModel.isClicked.drop(1).flowWithLifecycle(lifecycle).onEach {

            println("Прошло: $it")

            val currentPosition = trackViewModel.currentTrack.value.id - 1
            val previousPosition = trackViewModel.previousTrack.value.id - 1

            if (playingWhilePlaying) adapter.notifyItemChanged(previousPosition) else
                adapter.notifyItemChanged(currentPosition)

        }.launchIn(lifecycleScope)


    }

}


//                fun setHolderItemProperty(id: Int) = appBinding.list
//                    .findViewHolderForAdapterPosition(currentPosition)
//                    ?.itemView?.findViewById<MaterialButton>(id)
