package com.example.a2ndaccidentprevention.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlertDao {
    @Query("SELECT * FROM Alert")
    fun get(): LiveData<Alert>

    @Insert
    fun insert(alert:Alert)

    @Update
    fun update(alert:Alert)
}