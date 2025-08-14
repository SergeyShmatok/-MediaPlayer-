package ru.netology.mediaplayer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.netology.mediaplayer.adapter.OnInteractionListener
import ru.netology.mediaplayer.adapter.TrackAdapter
import ru.netology.mediaplayer.adapter.clicked
import ru.netology.mediaplayer.adapter.currentPosition
import ru.netology.mediaplayer.databinding.ActivityAppBinding
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

        val adapter = TrackAdapter(object : OnInteractionListener {

//                                    override fun getMetaData(url: String): String {
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

        }, observer, appBinding)

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

            val listSize = adapter.currentList.size
            fun scrollTo(num: Int) = appBinding.list.scrollToPosition(num)

            it.stop()
            it.reset()

            val position = if (currentPosition + 1 <= listSize - 1) currentPosition + 1 else 0


            lifecycleScope.launch {

                scrollTo(currentPosition)
                delay(50)
                clicked = false
                adapter.notifyItemChanged(currentPosition)
                delay(50)
                if (currentPosition < listSize - 1) scrollTo(currentPosition + 1)
                else scrollTo(0)
                delay(50)

                appBinding.list.findViewHolderForAdapterPosition(position)
                    ?.itemView?.findViewById<MaterialButton>(R.id.icon_play)?.performClick()

            }

        }

        appBinding.apply {

            fab.setOnClickListener { it ->

                when (observer.mediaPlayer?.isPlaying) {

                    false -> {

                        if (observer.pauseOn) {
                            fab.isChecked = true
                            clicked = true
                            adapter.notifyItemChanged(currentPosition)

                            observer.playAfterPause()

                        } else {
                            fab.isChecked = false

                        }

                    }

                    true -> {
                        fab.isChecked = false
                        clicked = false

                        adapter.notifyItemChanged(currentPosition)

                        observer.pause()

                    }

                    else -> {}
                }

            }
        }


    }

}


//                fun setHolderItemProperty(id: Int) = appBinding.list
//                    .findViewHolderForAdapterPosition(currentPosition)
//                    ?.itemView?.findViewById<MaterialButton>(id)
