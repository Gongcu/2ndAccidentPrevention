package com.example.a2ndaccidentprevention.room

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room

@Database(entities = [Alert::class], version = 1)
abstract class AlertDatabase :RoomDatabase(){
    abstract fun alertDao(): AlertDao

    companion object{
        private var INSTANCE:AlertDatabase?=null

        fun getInstance(context: Context):AlertDatabase?{
            if(INSTANCE==null){
                synchronized(AlertDatabase::class){
                    INSTANCE= Room.databaseBuilder(context.applicationContext, AlertDatabase::class.java, "alert")
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return INSTANCE
        }
    }
}