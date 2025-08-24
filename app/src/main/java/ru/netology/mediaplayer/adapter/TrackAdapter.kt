package ru.netology.mediaplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.mediaplayer.databinding.SongCardBinding
import ru.netology.mediaplayer.dto.Track
import javax.inject.Inject


interface OnInteractionListener {
    //    fun getMetaData(url: String): String
    fun getPosition(): Int
    fun getPreviousPosition(): Int
    fun isClicked(): Boolean
    fun onClick(track: Track, binding: SongCardBinding)
}

typealias ViewHolder = RecyclerView.ViewHolder
typealias Diff = DiffUtil.ItemCallback<Track>

//-------------------- PostAdapter -------------------

class TrackAdapter @Inject constructor(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Track, TrackHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val songCardBinding =
            SongCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return TrackHolder(
            songCardBinding
        )
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {

        val track = getItem(position)

//        val durations = onInteractionListener.getMetaData(url)

        holder.songCardBinding.apply {

            iconPlay.isChecked = false
            iconLike.isInvisible = true
            iconNot.isInvisible = true

            val currentPosition: Int = onInteractionListener.getPosition()

            val previousPosition: Int = onInteractionListener.getPreviousPosition()

            val clicked: Boolean = onInteractionListener.isClicked()

//            duration.text = durations

//             Три положения для разных ситуаций

            if (position == currentPosition && clicked) {
                iconPlay.isChecked = true
                iconLike.isVisible = true
                iconNot.isVisible = true
            }

            if (position == currentPosition && !clicked) {
                iconPlay.isChecked = false
                iconLike.isInvisible = true
                iconNot.isInvisible = true
            }

            if (position == previousPosition && !clicked) {
                iconPlay.isChecked = false
                iconLike.isInvisible = true
                iconNot.isInvisible = true
            }

            songTitle.text = track.file.substring(0, track.file.length - 4)

            iconPlay.setOnClickListener {

                onInteractionListener.onClick(track, holder.songCardBinding)

            }
        }


    }
}

//--------------------- PostHolder ---------------------

class TrackHolder(
    val songCardBinding: SongCardBinding,
) : ViewHolder(songCardBinding.root)

//---------------------- DiffUtil -----------------------

class TrackDiffCallback : Diff() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }
}

//------------------------- End
