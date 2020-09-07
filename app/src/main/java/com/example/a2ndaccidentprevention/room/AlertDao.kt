package com.example.a2ndaccidentprevention.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlertDao {
    @Query("SELECT * FROM Alert WHERE id=1")
    fun get(): LiveData<Alert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert:Alert)

    @Update
    suspend fun update(alert:Alert)

    @Query("UPDATE Alert SET vibration=:status  WHERE id=1")
    suspend fun updateVibration(status:Boolean)

    @Query("UPDATE Alert SET sound=:status  WHERE id=1")
    suspend fun updateSound(status:Boolean)
}