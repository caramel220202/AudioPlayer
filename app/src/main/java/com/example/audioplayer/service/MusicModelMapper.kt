package com.example.audioplayer.service

import com.example.audioplayer.MusicModel
import com.example.audioplayer.PlayerModel

fun MusicEntity.mapper(id:Long):MusicModel =
    MusicModel(
        id=id,
        streamUrl = streamUrl,
        coverUrl = coverUrl,
        track = track,
        artist = artist
    )


fun MusicDto.mapper():PlayerModel =
    PlayerModel(
        playMusicList = musics.mapIndexed{ index, musicEntity ->
            musicEntity.mapper(index.toLong())
        }
    )

