package com.example.audioplayer.service

import retrofit2.Call
import retrofit2.http.GET

interface AudioPlayerService {
    @GET("/v3/9f853a2a-62b3-48f4-91d5-65eabd0b32f5")
    fun getMusicsList():Call<MusicDto>
}