package com.sbs.a2ndaccidentprevention.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Alert(
    @PrimaryKey
    var id: Int,
    @ColumnInfo
    var vibration: Boolean,
    @ColumnInfo
    var sound:Boolean
){
    constructor():this(1,true,true)
}