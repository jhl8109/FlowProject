package com.example.flowproject

import com.google.gson.annotations.SerializedName
import retrofit2.http.Url

data class Image(
    @SerializedName("result")
    var result: Result,
)
