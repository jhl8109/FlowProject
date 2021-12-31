package com.example.flowproject

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_photos")
data class Gallery(
    @PrimaryKey(autoGenerate = true) val id : Int?,
    var photo : String
)