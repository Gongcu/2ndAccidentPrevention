package com.example.a2ndaccidentprevention.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Alert(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    @ColumnInfo
    var vibration: Int?,
    @ColumnInfo
    var sound:Int?
){
    constructor():this(null,0,0)
}