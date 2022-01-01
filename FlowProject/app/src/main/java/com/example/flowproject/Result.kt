package com.example.flowproject

import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("faces")
    val faces:List<Face>
)
