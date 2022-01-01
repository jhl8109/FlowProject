package com.example.flowproject

import retrofit2.Call
import retrofit2.http.*


interface RetrofitService {
    @FormUrlEncoded
    @POST("v2/vision/face/detect")
    fun getOnlineChannel(
        @Header("Authorization") key : String,
        @Field("image_url") source :String
    ): Call<Image>
}
