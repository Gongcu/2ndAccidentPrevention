package com.example.a2ndaccidentprevention

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.example.a2ndaccidentprevention.MainActivity.GLOBAL.token
import com.example.a2ndaccidentprevention.databinding.ActivityMainBinding
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import com.example.a2ndaccidentprevention.room.Alert
import com.example.a2ndaccidentprevention.util.PermissionUtil
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var auth: FirebaseAuth
    object GLOBAL{
        var token: String = ""
    }
    private val viewModel: ViewModel by viewModels()
    private lateinit var sensorManager : SensorManager
    private lateinit var accelerometer : Sensor

    private lateinit var permissionUtil: PermissionUtil

    private var accident = false

    lateinit var soundSwitch: Switch
    lateinit var vibrationSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_main)
        binding.viewmodel = viewModel

        permissionUtil= PermissionUtil(applicationContext, object:PermissionListener{
            override fun onPermissionGranted() {
                mapViewInit(map_view)
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
            }
        })
        permissionUtil.requestPermission()

        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        auth = FirebaseAuth.getInstance()
        token = intent.getStringExtra("token").toString()





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
                drawerLayout.openDrawer(Gravity.LEFT)
            }else{
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        }

        val menu = navigationView.menu
        soundSwitch= menu.findItem(R.id.sound_alert_item).actionView.findViewById(R.id.drawer_sound) as Switch
        vibrationSwitch = menu.findItem(R.id.vibration_alert_item).actionView.findViewById(R.id.drawer_vibrator) as Switch
        soundSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
        vibrationSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
        viewModel.get().observe(this, Observer<Alert> {
            if(it==null){
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    viewModel.insert(Alert(id = 1,vibration = true,sound = true))
                }
            }else{
                Log.d("alert", it.toString())
                soundSwitch.isChecked=it.sound
                vibrationSwitch.isChecked=it.vibration
            }
        })

    }

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            when (buttonView.id) {
                R.id.drawer_vibrator -> {
                    viewModel.updateVibration(isChecked)
                }
                R.id.drawer_sound -> {
                    viewModel.updateSound(isChecked)
                }
            }
        }
    }

    private fun removeToken() {
        if(token != "")
            viewModel.deleteLocation(token)
    }

    private fun mapViewInit(mapView: MapView){
        mapView.setZoomLevel(2, true)
        mapView.zoomIn(true)
        mapView.zoomOut(true)
        mapView.isHDMapTileEnabled = false //고해상도 지도 사용 안함
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        mapView.setCurrentLocationEventListener(viewModel)
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.8f
        val gravity = FloatArray(3)
        var total:Double = 0.0

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2] //중력가속도 계산

        val accX: Float = event.values[0] - gravity[0]
        val accY: Float = event.values[1] - gravity[1]
        val accZ: Float = event.values[2] - gravity[2] // 중력을 뺀 가속도.

        total = sqrt(accX.toDouble().pow(2.0) + accY.toDouble().pow(2.0) + accZ.toDouble().pow(2.0))

        //&& isLocateChanged(currLocation,prevLocation)
        if(total>1.0*9.8 && token.isNotEmpty() && !accident) {
            viewModel.notifyAccident()
            accident=true
        }
    }
    private fun isLocateChanged(location1: Location, location2: Location):Boolean{
        return location1.distanceTo(location2)>0
    }
    override fun onPause() {
        super.onPause()
        removeToken()
    }
}
