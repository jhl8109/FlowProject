package com.example.flowproject

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object FormDataUtil {
    fun getImageBody(name: String, file: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            name = name,
            filename = file.name,
            body = file.asRequestBody("image/*".toMediaType())
        )
    }
}