package com.example.a2ndaccidentprevention

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
import android.view.Gravity
import androidx.core.content.ContextCompat.getSystemService
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import com.example.a2ndaccidentprevention.room.Alert
import com.example.a2ndaccidentprevention.view.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class ViewModel(application: Application) : AndroidViewModel(application),MapView.CurrentLocationEventListener {
    val acceleratorLiveData = AcceleratorLiveData()
    private var isFirst = true
    private var queue :Queue<LocationInfo> = LinkedList()
    private val currLocation = Location("currLocation")
    private val prevLocation = Location("prevLocation")
    private var currentBearing = 0.0f
    val mapPoint: MutableLiveData<MapPoint> by lazy{
        MutableLiveData<MapPoint>()
    }
    private val ioScope = CoroutineScope(Dispatchers.IO)
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


    //변화를 감지하기 위한 LiveData<?> 상속 observing 할 시 ?에 선언한 타입을 반환한다.
    inner class AcceleratorLiveData: LiveData<Any>(), SensorEventListener{
        private val sensorManager:SensorManager
            get()=getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        private var accident = false

        override fun onSensorChanged(event: SensorEvent) {
            val alpha = 0.8f
            val gravity = FloatArray(3)
            val acceleration = FloatArray(3)
            var total:Double = 0.0

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2] //중력가속도 계산

            acceleration[0]= event.values[0] - gravity[0]
            acceleration[1]= event.values[1] - gravity[1]
            acceleration[2]= event.values[2] - gravity[2] // 중력을 뺀 가속도.

            total = sqrt(acceleration[0].toDouble().pow(2.0) + acceleration[1].toDouble().pow(2.0) + acceleration[2].toDouble().pow(2.0))

            if(total>2.0*9.8 && MainActivity.GLOBAL.token.isNotEmpty() && !accident ) {
                ioScope.launch {
                    notifyAccident()
                    accident=true
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        //Observer 상태가 start, resume 시에 호출된다.
        override fun onActive() {
            super.onActive()
            sensorManager.let { sm ->
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).let{
                    sm.registerListener(this,it,SensorManager.SENSOR_DELAY_NORMAL)
                }
            }
        }
        //Observer 상태가 stop, pause 시에 호출된다.
        override fun onInactive() {
            super.onInactive()
            sensorManager.unregisterListener(this)
            Log.d("accelerator sensor","workFinished")
        }
    }
}