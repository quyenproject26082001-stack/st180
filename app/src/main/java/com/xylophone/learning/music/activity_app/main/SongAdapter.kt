package com.xylophone.learning.music.activity_app.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xylophone.learning.music.R
import com.xylophone.learning.music.data.model.Song
import com.xylophone.learning.music.databinding.ItemSongBinding

class SongAdapter(
    private var songs: List<Song>,
    private val onSongClick: (Song?) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    fun getSelectedSong(): Song? {
        return if(selectedPosition in songs.indices) songs[selectedPosition] else null
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {



        fun bind(song: Song, isSelected: Boolean) {
            binding.apply {
                tvSongName.text = song.name
                tvSongName.isSelected = isSelected

                root.setBackgroundResource(
                    if(isSelected) R.drawable.bg_btn_selected
                    else R.drawable.bg_btn_unselected
                )

                root.setOnClickListener {
                    val oldPos = selectedPosition
                    val newPos = bindingAdapterPosition
                    if(newPos == RecyclerView.NO_POSITION) return@setOnClickListener



                    selectedPosition = newPos


                    if(oldPos != RecyclerView.NO_POSITION) notifyItemChanged(oldPos)
                    notifyItemChanged(newPos)
                    onSongClick(getSelectedSong())
                }


            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(

            song = songs[position],
            isSelected = position == selectedPosition
            )
    }

    override fun getItemCount(): Int = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
        onSongClick(null)
    }

    // Select song by ID and return position
    fun selectSongById(songId: String): Int {
        val position = songs.indexOfFirst { it.id == songId }
        if (position != RecyclerView.NO_POSITION) {
            selectedPosition = position
            notifyItemChanged(position)
            onSongClick(songs[position])
        }
        return position
    }

    // Get selected position
    fun getSelectedPosition(): Int = selectedPosition
}
