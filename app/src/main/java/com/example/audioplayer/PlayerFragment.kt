package com.example.audioplayer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.audioplayer.adapter.PlayListAdapter
import com.example.audioplayer.databinding.FragmentPlayerBinding
import com.example.audioplayer.service.AudioPlayerService
import com.example.audioplayer.service.MusicDto
import com.example.audioplayer.service.mapper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment(R.layout.fragment_player) {
    private var model: PlayerModel = PlayerModel()
    private var binding: FragmentPlayerBinding? = null
    private lateinit var adapter: PlayListAdapter
    private var player: ExoPlayer? = null
    private val updateSeekRunnable = Runnable {
        updateSeek()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)
        binding = fragmentPlayerBinding

        initPlayControlButtons(fragmentPlayerBinding)
        initPlayView(fragmentPlayerBinding)
        initRecyclerView()
        initPlayListButton(fragmentPlayerBinding)
        initRetrofitService()
        initSeekBar(fragmentPlayerBinding)
    }

    private fun initPlayControlButtons(fragmentPlayerBinding: FragmentPlayerBinding) {

        player?.let { player ->
            fragmentPlayerBinding.playControlBtn.setOnClickListener {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }
        fragmentPlayerBinding.nextBtn.setOnClickListener {
            if (model.isShuffleMode) {
                val shuffleMusic = model.shuffleMusic() ?: return@setOnClickListener
                playMusic(shuffleMusic)

            } else {
                val nextMusic = model.nextMusic() ?: return@setOnClickListener
                playMusic(nextMusic)
            }

        }

        fragmentPlayerBinding.previousBtn.setOnClickListener {
            if (model.isShuffleMode) {
                val shuffleMusic = model.shuffleMusic() ?: return@setOnClickListener
                playMusic(shuffleMusic)
            } else {
                val prevMusic = model.prevMusic() ?: return@setOnClickListener
                playMusic(prevMusic)
            }

        }
        fragmentPlayerBinding.repeatBtn.setOnClickListener {
            if (!model.isRepeatMode) {
                binding?.let {
                    it.repeatBtn.setColorFilter(Color.BLACK)
                }
                player?.repeatMode = Player.REPEAT_MODE_ONE
                model.isRepeatMode = true
                Toast.makeText(context, "Repeat On", Toast.LENGTH_SHORT).show()

                Log.d("testt", model.isRepeatMode.toString())
            } else {
                model.isRepeatMode = false
                binding?.let {
                    it.repeatBtn.setColorFilter(R.color.gray_aa)
                }
                player?.repeatMode = Player.REPEAT_MODE_OFF
                Log.d("testt", model.isRepeatMode.toString())
                Toast.makeText(context, "Repeat off", Toast.LENGTH_SHORT).show()


            }
        }
        fragmentPlayerBinding.shuffleBtn.setOnClickListener {
            if (!model.isShuffleMode) {
                binding?.let {
                    it.shuffleBtn.setColorFilter(Color.BLACK)
                }
                Toast.makeText(context, "Shuffle On", Toast.LENGTH_SHORT).show()
                player?.shuffleModeEnabled = true
                model.isShuffleMode = true
            } else {
                model.isShuffleMode = false
                binding?.let {
                    it.shuffleBtn.setColorFilter(R.color.gray_aa)
                }
                player?.shuffleModeEnabled = false
                Toast.makeText(context, "Shuffle Off", Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun initPlayView(fragmentPlayerBinding: FragmentPlayerBinding) {
        context?.let {
            player = ExoPlayer.Builder(it).build()
        }
        fragmentPlayerBinding.player.player = player

        binding?.let { binding ->
            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)

                    if (isPlaying) {
                        binding.playControlBtn.setImageResource(R.drawable.ic_pause_24)
                    } else {
                        binding.playControlBtn.setImageResource(R.drawable.ic_play_arrow_24)
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)

                    updateSeek()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)

                    val newIndex = mediaItem?.mediaId ?: return
                    model.currentPosition = newIndex.toInt()

                    updatePlayerView(model.currentMusicModel())
                    updateRecyclerviewFocus()
                    adapter.submitList(model.getAdapterModels())
                }
            })
        }
    }

    private fun updateRecyclerviewFocus() {
        binding?.let { binding ->
            binding.playListRecyclerView.scrollToPosition(model.currentPosition)
        }
    }

    private fun updateSeek() {

        val player = this.player ?: return
        val duration = if (player.duration >= 0) player.duration else 0
        val position = player.currentPosition

        updateSeekUi(duration, position)
        val state = player.playbackState

        view?.removeCallbacks(updateSeekRunnable)

        if (state != Player.STATE_IDLE && state != Player.STATE_ENDED) {
            view?.postDelayed(updateSeekRunnable, 1000)
        }
    }

    private fun updateSeekUi(duration: Long, position: Long) {
        binding?.let { binding ->
            binding.playerSeekBar.max = (duration / 1000).toInt()
            binding.playerSeekBar.progress = (position / 1000).toInt()
            binding.playListSeekBar.max = (duration / 1000).toInt()
            binding.playListSeekBar.progress = (position / 1000).toInt()

            binding.totalTimeTextView.text = String.format(
                "%02d:%02d", TimeUnit.MINUTES
                    .convert(duration, TimeUnit.MILLISECONDS), (duration / 1000) % 60
            )

            binding.playTimeTextView.text = String.format(
                "%02d:%02d", TimeUnit.MINUTES
                    .convert(position, TimeUnit.MILLISECONDS), (position / 1000) % 60
            )

        }
    }

    private fun updatePlayerView(currentMusicModel: MusicModel?) {
        currentMusicModel ?: return
        binding?.let {
            it.artist.text = currentMusicModel.artist
            it.trackTextView.text = currentMusicModel.track
            Glide.with(it.coverImageView.context).load(currentMusicModel.coverUrl)
                .into(it.coverImageView)
        }
    }

    private fun initPlayListButton(fragmentPlayerBinding: FragmentPlayerBinding) {

        fragmentPlayerBinding.playListBtn.setOnClickListener {
            if (model.currentPosition == -1) return@setOnClickListener

            fragmentPlayerBinding.playListGroup.isVisible = model.isWatchingPlayListView
            fragmentPlayerBinding.playerViewGroup.isVisible = model.isWatchingPlayListView.not()

            model.isWatchingPlayListView = !model.isWatchingPlayListView
        }
    }

    private fun initRecyclerView() {
        binding?.let { fragmentBinding ->
            adapter = PlayListAdapter { musicModel ->
                fragmentBinding.trackTextView.text = musicModel.track
                fragmentBinding.artist.text = musicModel.artist
                Glide.with(fragmentBinding.coverImageView.context).load(musicModel.coverUrl)
                    .into(fragmentBinding.coverImageView)

                playMusic(musicModel)

            }
            fragmentBinding.playListRecyclerView.adapter = adapter
            fragmentBinding.playListRecyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    private fun initRetrofitService() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(AudioPlayerService::class.java).also {
            it.getMusicsList()
                .enqueue(object : Callback<MusicDto> {
                    override fun onResponse(
                        call: Call<MusicDto>,
                        response: Response<MusicDto>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let { dto ->
                                model = dto.mapper()
                                adapter.submitList(model.getAdapterModels())
                                setMusicList(model.getAdapterModels())
                            }
                        }
                    }

                    override fun onFailure(call: Call<MusicDto>, t: Throwable) {
                        Log.d("testt", t.message.toString())
                    }
                })
        }
    }

    private fun setMusicList(modelList: List<MusicModel>) {
        context?.let {
            player?.addMediaItems(modelList.map { musicModel ->
                MediaItem.Builder()
                    .setMediaId(musicModel.id.toString())
                    .setUri(musicModel.streamUrl)
                    .build()
            })
            player?.prepare()
        }
    }

    private fun playMusic(musicModel: MusicModel) {
        model.updateCurrentPosition(musicModel)
        Log.d("testt", model.currentPosition.toString())
        player?.seekTo(model.currentPosition, 0)
        player?.play()
    }

    private fun initSeekBar(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playerSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player?.seekTo((seekBar.progress * 1000).toLong())
            }
        })

        fragmentPlayerBinding.playListSeekBar.setOnTouchListener { v, event ->
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        player?.release()
        player = null
        view?.removeCallbacks(updateSeekRunnable)
    }

    override fun onPause() {
        super.onPause()
        player?.play()
    }

    companion object {
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }
}