package com.example.flowproject

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id : Int?,
    var name: String, var address: String, var phonenumber: String, var photo: String, var uri: String
)