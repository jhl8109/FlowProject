package com.example.flowproject

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {
    @Multipart
    @POST("v2/vision/face/detect")
    fun getOnlineChannel(
        @Header("Authorization") key : String,
        @Part image : MultipartBody.Part?
    ): Call<Image>
}
