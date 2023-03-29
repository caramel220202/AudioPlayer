package com.example.audioplayer.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.audioplayer.MusicModel
import com.example.audioplayer.databinding.ItemPlaylistBinding
import com.example.audioplayer.service.MusicEntity

class PlayListAdapter(private val callback: (MusicModel) -> Unit):ListAdapter<MusicModel,PlayListAdapter.PlayListViewHolder>(diffUtil) {

    inner class PlayListViewHolder(val binding:ItemPlaylistBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(musicModel: MusicModel){
            binding.trackTextView.text = musicModel.track
            binding.artistTextView.text = musicModel.artist
            Glide.with(binding.coverImageView.context)
                .load(musicModel.coverUrl)
                .into(binding.coverImageView)

            if (musicModel.isPlaying){
                itemView.setBackgroundColor(Color.GRAY)
            }else{
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            itemView.setOnClickListener {
                callback(musicModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
    return PlayListViewHolder(ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
    companion object{
        val diffUtil = object :DiffUtil.ItemCallback<MusicModel>(){
            override fun areItemsTheSame(oldItem: MusicModel, newItem: MusicModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MusicModel, newItem: MusicModel): Boolean {
            return oldItem == newItem
            }
        }
    }
}