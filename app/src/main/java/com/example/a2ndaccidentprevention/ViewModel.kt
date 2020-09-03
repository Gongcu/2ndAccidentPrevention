package com.example.a2ndaccidentprevention

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.a2ndaccidentprevention.retrofit.Location
import com.example.a2ndaccidentprevention.room.Alert
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)

    fun get(): LiveData<Alert> {
        return repository.get()
    }

    fun update(alert: Alert) {
        repository.update(alert)
    }

    fun insert(alert: Alert) {
        repository.insert(alert)
    }

    fun postLocation(location: Location){
        repository.postLocation(location)
    }
    fun notifyAccident(locationList: Queue<Location>){
        repository.notifyAccident(locationList)
    }
    fun deleteLocation(token: String){
        repository.deleteLocation(token)
    }
}