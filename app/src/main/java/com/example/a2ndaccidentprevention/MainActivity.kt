package com.example.a2ndaccidentprevention

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.AudioManager
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import com.example.a2ndaccidentprevention.room.Alert
import com.example.a2ndaccidentprevention.util.PermissionUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_switch_sound.*
import kotlinx.android.synthetic.main.layout_switch_vibrator.*
import kotlinx.android.synthetic.main.layout_switch_vibrator.view.*
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), MapView.CurrentLocationEventListener, SensorEventListener {
    private var isFirst = true
    private lateinit var auth: FirebaseAuth
    private  var token: String = ""
    private lateinit var uid: String
    val viewModel: ViewModel by viewModels()

    private lateinit var sensorManager : SensorManager
    private lateinit var accelerometer : Sensor
    private lateinit var vibrator: Vibrator
    private lateinit var audioManager: AudioManager

    private val gravity = FloatArray(3)
    private var total:Double = 0.0

    private lateinit var permissionUtil: PermissionUtil

    private var queue :Queue<LocationInfo> = LinkedList()

    private val currLocation = Location("currLocation")
    private val prevLocation = Location("prevLocation")
    private var currentBearing = 0.0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionUtil= PermissionUtil(applicationContext)
        permissionUtil.requestPermission()

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        auth = FirebaseAuth.getInstance()
        token = intent.getStringExtra("token").toString()
        uid = intent.getStringExtra("uid").toString()

        Log.e("token",token)

        mapViewInit(map_view)

        navigationView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.logout_item) {
                auth.signOut()
                removeToken()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                false
            }
            false
        }
        settingBtn.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.openDrawer(Gravity.LEFT) ;
            }else{
                drawerLayout.closeDrawer(Gravity.LEFT); ;
            }
        }
        //drawer_sound.setOnCheckedChangeListener(onCheckedChangeListener)  --> null error ocurr
        //drawer_vibrator.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        when (buttonView.id) {
            R.id.drawer_sound -> if (isChecked) {
                Toast.makeText(applicationContext, "sound on", Toast.LENGTH_SHORT).show()
                //soundCheck = 1
            } else {
                Toast.makeText(applicationContext, "sound off", Toast.LENGTH_SHORT).show()
                //soundCheck = 0
            }
            R.id.drawer_vibrator -> if (isChecked) {
                Toast.makeText(applicationContext, "vibration on", Toast.LENGTH_SHORT).show()
                //vibratorCheck = 1
            } else {
                Toast.makeText(applicationContext, "vibration off", Toast.LENGTH_SHORT).show()
                //vibratorCheck = 0
            }
        }
    }

    override fun onCurrentLocationUpdate(mapView: MapView, mapPoint: MapPoint, v: Float) {
        val mapPointGeo = mapPoint.mapPointGeoCoord
        val latitude = mapPointGeo.latitude
        val longitude = mapPointGeo.longitude
        var locationInfo = LocationInfo(token, latitude, longitude,0.0)

        if(isFirst && mapPointGeo!=null){
            setLocation(currLocation, latitude, longitude)
            queue.add(locationInfo)
            viewModel.postLocation(locationInfo)
            isFirst=!isFirst
        }else{
            prevLocation.set(currLocation)
            setLocation(currLocation, latitude, longitude)

            currentBearing = prevLocation.bearingTo(currLocation)
            locationInfo.bearing = currentBearing.toDouble()
            bearingTextView.text = currentBearing.toString()

            if(queue.size<10){
                queue.add(locationInfo)
            }else{
                queue.poll()
                queue.add(locationInfo)
            }

            viewModel.postLocation(locationInfo)
        }

        mapViewUpdater(mapView, mapPoint)
    }



    private fun removeToken() {
        if(token != "")
            viewModel.deleteLocation(token)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.8f
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2] //중력가속도 계산


        val accX: Float = event.values[0] - gravity[0]
        val accY: Float = event.values[1] - gravity[1]
        val accZ: Float = event.values[2] - gravity[2] // 중력을 뺀 가속도.


        total = sqrt(accX.toDouble().pow(2.0) + accY.toDouble().pow(2.0) + accZ.toDouble().pow(2.0))


        if(total>2.0*9.8 && token!="" && isLocateChanged(currLocation,prevLocation))
            viewModel.notifyAccident(queue)
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

    private fun mapViewInit(mapView: MapView){
        mapView.setZoomLevel(2, true);
        mapView.zoomIn(true);
        mapView.zoomOut(true);
        mapView.isHDMapTileEnabled = false; //고해상도 지도 사용 안함
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading;
        mapView.setCurrentLocationEventListener(this);
    }

    private fun isLocateChanged(location1: Location, location2: Location):Boolean{
        return location1.distanceTo(location2)>0
    }

    override fun onPause() {
        super.onPause()
        removeToken()
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {}
    override fun onCurrentLocationUpdateFailed(p0: MapView?) {}
    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {}
}
