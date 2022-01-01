package com.example.flowproject

import com.google.gson.annotations.SerializedName

data class FaceAttr(
    @SerializedName("gender")
    val gender : Gender,
    @SerializedName("age")
    val age:Float
    )
