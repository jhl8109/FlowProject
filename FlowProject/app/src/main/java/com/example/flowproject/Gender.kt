package com.example.flowproject

import com.google.gson.annotations.SerializedName

data class Gender(
    @SerializedName("male")
    val male : Float,
    @SerializedName("female")
    val female : Float
)
