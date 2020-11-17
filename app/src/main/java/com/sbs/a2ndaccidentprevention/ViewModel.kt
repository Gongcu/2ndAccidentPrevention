package com.sbs.a2ndaccidentprevention

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sbs.a2ndaccidentprevention.retrofit.LocationInfo
import com.sbs.a2ndaccidentprevention.room.Alert
import com.sbs.a2ndaccidentprevention.view.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.util.*


class ViewModel(application: Application) : AndroidViewModel(application),MapView.CurrentLocationEventListener {
    private var startTime:Long = 0
    private var endTime:Long = 0
    private var isFirst = true
    private var queue :Queue<LocationInfo> = LinkedList()
    private val currLocation = Location("currLocation")
    private val prevLocation = Location("prevLocation")
    private var currentBearing = 0.0f
    val mapPoint: MutableLiveData<MapPoint> by lazy{
        MutableLiveData<MapPoint>()
    }
    private val speedLData:MutableLiveData<Double> = MutableLiveData<Double>()
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val repository = Repository(application)

    fun get(): LiveData<Alert> {
        return repository.get()
    }

    fun getSpeed():MutableLiveData<Double>{
        return speedLData
    }

    suspend fun updateVibration(status: Boolean){
        repository.updateVibration(status)
    }
    suspend fun updateSound(status: Boolean){
        repository.updateSound(status)
    }
    suspend fun insert(alert: Alert) {
        repository.insert(alert)
    }

    private fun postLocation(locationInfo: LocationInfo){
        repository.postLocation(locationInfo)
    }
    fun notifyAccident(){
        if(isLocateChanged(currLocation,prevLocation))
            repository.notifyAccident(LocationInfo(MainActivity.GLOBAL.token,currLocation.latitude,currLocation.longitude,currentBearing),queue)
    }
    fun deleteLocation(token: String){
        repository.deleteLocation(token)
    }

    private fun isLocateChanged(location1: Location, location2: Location):Boolean{
        return location1.distanceTo(location2)>0
    }

    override fun onCurrentLocationUpdate(mapView: MapView, mapPoint: MapPoint, v: Float) {
        this.mapPoint.value=mapPoint
        val mapPointGeo = mapPoint.mapPointGeoCoord
        val latitude = mapPointGeo.latitude
        val longitude = mapPointGeo.longitude
        val locationInfo = LocationInfo(MainActivity.GLOBAL.token, latitude, longitude, 0.0f)

        if(isFirst && mapPointGeo!=null){
            startTime=System.currentTimeMillis()
            setLocation(currLocation, latitude, longitude)
            queue.add(locationInfo)
            ioScope.launch {
                postLocation(locationInfo)
            }
            isFirst=!isFirst
        }else{
            prevLocation.set(currLocation)
            setLocation(currLocation, latitude, longitude)

            currentBearing = prevLocation.bearingTo(currLocation)
            locationInfo.bearing = currentBearing

            if(queue.size<10){
                queue.add(locationInfo)
            }else{
                queue.poll()
                queue.add(locationInfo)
            }
            ioScope.launch {
                postLocation(locationInfo)
            }

            endTime=System.currentTimeMillis()
            val speed :Double=(currLocation.distanceTo(prevLocation)) / ((endTime - startTime) / 1000) * 3.6;
            if(!speed.isNaN())
                speedLData.value = speed
        }
    }

    private fun setLocation(location: Location, latitude: Double, longitude: Double){
        location.latitude = latitude
        location.longitude = longitude
    }

    fun getBearing():Float{
        return currentBearing
    }



    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {}
    override fun onCurrentLocationUpdateFailed(p0: MapView?) {}
    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {}
}