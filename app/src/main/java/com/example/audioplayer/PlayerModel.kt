package com.example.audioplayer

import android.util.Log
import kotlin.random.Random

data class PlayerModel (
    private val playMusicList:List<MusicModel> = emptyList(),
    var currentPosition:Int = -1,
    var isWatchingPlayListView:Boolean = true,
    var isShuffleMode:Boolean = false,
    var isRepeatMode:Boolean = false
        ){
    fun getAdapterModels():List<MusicModel>{
        return playMusicList.mapIndexed { index, musicModel ->
            val newItem = musicModel.copy(
                isPlaying = index == currentPosition
            )
            newItem
        }
    }
    fun updateCurrentPosition(musicModel: MusicModel){
        currentPosition = playMusicList.indexOf(musicModel)
        Log.d("testt","update")
    }
    fun nextMusic():MusicModel?{
        if (playMusicList.isEmpty()) return null

        currentPosition = if ((currentPosition +1) == playMusicList.size) 0 else currentPosition +1
        return playMusicList[currentPosition]
        Log.d("testt","next")

    }

    fun prevMusic():MusicModel?{
        if (playMusicList.isEmpty()) return null
        currentPosition = if ((currentPosition -1) < 0) playMusicList.lastIndex else currentPosition -1

        return playMusicList[currentPosition]
        Log.d("testt","prev")

    }

    fun shuffleMusic():MusicModel?{
        if (playMusicList.isEmpty()) return null

        currentPosition = Random.nextInt(0,playMusicList.size -1)

        return playMusicList[currentPosition]
    }


    fun currentMusicModel():MusicModel?{
        if (playMusicList.isEmpty()) return null
        return playMusicList[currentPosition]
    }
}

