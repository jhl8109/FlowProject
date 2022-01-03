package com.example.flowproject

import androidx.room.*

@Dao
interface ContactDao {
    @Query("SELECT * FROM tb_contacts")
    fun getAll(): List<Contact>

    @Insert
    fun insertAll(vararg contact: Contact)

    @Update
    fun updateUsers(vararg contact: Contact)

    @Delete
    fun delete(contact: Contact)
}