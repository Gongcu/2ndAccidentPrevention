package com.example.a2ndaccidentprevention

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import com.example.a2ndaccidentprevention.room.Alert
import kotlinx.android.synthetic.main.activity_main.*
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class ViewModel(application: Application) : AndroidViewModel(application),MapView.CurrentLocationEventListener {
    private var isFirst = true

    private var queue :Queue<LocationInfo> = LinkedList()
    private val currLocation = Location("currLocation")
    private val prevLocation = Location("prevLocation")
    private var currentBearing = 0.0f

    private val repository = Repository(application)

    fun get(): LiveData<Alert> {
        return repository.get()
    }

    suspend fun update(alert: Alert) {
        repository.update(alert)
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

    fun postLocation(locationInfo: LocationInfo){
        repository.postLocation(locationInfo)
    }
    fun notifyAccident(){
        repository.notifyAccident(LocationInfo(MainActivity.GLOBAL.token,currLocation.latitude,currLocation.longitude,currentBearing),queue)
    }
    fun deleteLocation(token: String){
        repository.deleteLocation(token)
    }

    override fun onCurrentLocationUpdate(mapView: MapView, mapPoint: MapPoint, v: Float) {
        val mapPointGeo = mapPoint.mapPointGeoCoord
        val latitude = mapPointGeo.latitude
        val longitude = mapPointGeo.longitude
        var locationInfo = LocationInfo(MainActivity.GLOBAL.token, latitude, longitude, 0.0f)

        if(isFirst && mapPointGeo!=null){
            setLocation(currLocation, latitude, longitude)
            queue.add(locationInfo)
            postLocation(locationInfo)
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
            postLocation(locationInfo)
        }

        mapViewUpdater(mapView, mapPoint)
    }
    private fun setLocation(location: Location, latitude: Double, longitude: Double){
        location.latitude = latitude
        location.longitude = longitude
    }

    private fun mapViewUpdater(mapView: MapView, mapPoint: MapPoint){
        mapView.setMapCenterPoint(mapPoint, true);
        mapView.setCurrentLocationRadius(250); //m단위  250이란 값이 실제 지도에서 1km정도에 해당됨
        mapView.setCurrentLocationRadiusFillColor(android.graphics.Color.argb(10, 255, 0, 0));
        mapView.setCurrentLocationRadiusStrokeColor(android.graphics.Color.argb(100, 255, 0, 0));
    }
    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {}
    override fun onCurrentLocationUpdateFailed(p0: MapView?) {}
    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {}
}