package com.example.a2ndaccidentprevention

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import com.example.a2ndaccidentprevention.room.Alert
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

    fun postLocation(locationInfo: LocationInfo){
        repository.postLocation(locationInfo)
    }
    fun notifyAccident(locationInfoList: Queue<LocationInfo>){
        repository.notifyAccident(locationInfoList)
    }
    fun deleteLocation(token: String){
        repository.deleteLocation(token)
    }
}