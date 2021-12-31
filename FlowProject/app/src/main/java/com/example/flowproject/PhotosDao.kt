package com.example.flowproject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotosDao {
    @Query("SELECT * FROM tb_photos")
    fun getAll(): List<Gallery>

    @Insert
    fun insertAll(vararg gallery: Gallery)

    @Delete
    fun delete(gallery: Gallery)
}