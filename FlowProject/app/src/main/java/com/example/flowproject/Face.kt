package com.example.flowproject

import com.google.gson.annotations.SerializedName

data class Face(
    @SerializedName("facial_attributes")
    val faceAttr : FaceAttr
)
