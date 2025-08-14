package ru.netology.mediaplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.mediaplayer.BuildConfig.BASE_URL
import ru.netology.mediaplayer.MediaLifecycleObserver
import ru.netology.mediaplayer.databinding.ActivityAppBinding
import ru.netology.mediaplayer.databinding.SongCardBinding
import ru.netology.mediaplayer.dto.Track
import javax.inject.Inject


interface OnInteractionListener {
//    fun getMetaData(url: String): String
}

const val URL = BASE_URL

var currentPosition: Int = 0

var previousPosition: Int = 0

var clicked: Boolean = false

typealias ViewHolder = RecyclerView.ViewHolder
typealias Diff = DiffUtil.ItemCallback<Track>

//-------------------- PostAdapter -------------------

class TrackAdapter @Inject constructor(
    private val onInteractionListener: OnInteractionListener,
    private val observer: MediaLifecycleObserver,
    private val activityBinding: ActivityAppBinding,
) : ListAdapter<Track, TrackHolder>(PointDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val songCardBinding =
            SongCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return TrackHolder(
            songCardBinding
        )
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {

        val track = getItem(position)

        val url = "$URL${track.file}"

//        val durations = onInteractionListener.getMetaData(url)

        holder.songCardBinding.apply {

            songTitle.text = track.file.substring(0, track.file.length - 4)

//            duration.text = durations

            iconPlay.setOnClickListener {

                previousPosition = currentPosition

                currentPosition = track.id - 1

                fun settingIconsParameters() {
                    iconPlay.isChecked = true
                    activityBinding.fab.isChecked = true

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

                when (observer.mediaPlayer?.isPlaying) {

                    false -> {

                        settingIconsParameters()

                        if (!observer.pauseOn) {
                            observer.play(url)
                        }

                        if (observer.pauseOn && currentPosition != previousPosition) {
                            observer.mediaPlayer?.stop()
                            observer.mediaPlayer?.reset()
                            observer.play(url)
                        }

                        if (observer.pauseOn && currentPosition == previousPosition) {
                            observer.playAfterPause()
                        }


                    }

                    true -> {

                        if (currentPosition == previousPosition) {
                            iconPlay.isChecked = false
                            activityBinding.fab.isChecked = false

                            iconLike.visibility = View.INVISIBLE
                            iconNot.visibility = View.INVISIBLE

                            observer.pause()
                        }

                        if (currentPosition != previousPosition) {

                            clicked = false
                            notifyItemChanged(previousPosition)

                            observer.mediaPlayer?.stop()
                            observer.mediaPlayer?.reset()

                            settingIconsParameters()

                            observer.play(url)
                        }

                    }

                    else -> {}
                }

            }
        }

        // Три условия для разных ситуаций

        if (position == currentPosition && clicked) {
            holder.songCardBinding.iconPlay.isChecked = true
            holder.songCardBinding.iconLike.isVisible = true
            holder.songCardBinding.iconNot.isVisible = true
        }

        if (position == currentPosition && !clicked) {
            holder.songCardBinding.iconPlay.isChecked = false
            holder.songCardBinding.iconLike.isInvisible = true
            holder.songCardBinding.iconNot.isInvisible = true
        }

        if (position == previousPosition && !clicked) {
            holder.songCardBinding.iconPlay.isChecked = false
            holder.songCardBinding.iconLike.isInvisible = true
            holder.songCardBinding.iconNot.isInvisible = true
        }

    }
}

//--------------------- PostHolder ---------------------

class TrackHolder(
    val songCardBinding: SongCardBinding,
) : ViewHolder(songCardBinding.root)

//---------------------- DiffUtil -----------------------

class PointDiffCallback : Diff() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }
}

//------------------------- End
