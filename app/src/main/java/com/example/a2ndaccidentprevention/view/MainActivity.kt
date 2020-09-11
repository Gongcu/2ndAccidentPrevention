package com.example.a2ndaccidentprevention.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.a2ndaccidentprevention.R
import com.example.a2ndaccidentprevention.ViewModel
import com.example.a2ndaccidentprevention.view.MainActivity.GLOBAL.token
import com.example.a2ndaccidentprevention.databinding.ActivityMainBinding
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import com.example.a2ndaccidentprevention.room.Alert
import com.example.a2ndaccidentprevention.util.AccidentReceiver
import com.example.a2ndaccidentprevention.util.AlertGenerator
import com.example.a2ndaccidentprevention.util.MyFirebaseMessagingService
import com.example.a2ndaccidentprevention.util.PermissionUtil
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapView
import java.io.Serializable
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener {
    private val viewModel: ViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager : SensorManager
    private lateinit var accelerometer : Sensor
    private lateinit var accidentReceiver : AccidentReceiver
    private lateinit var permissionUtil: PermissionUtil

    private var accident = false

    lateinit var soundSwitch: Switch
    lateinit var vibrationSwitch: Switch

    private val ioScope = CoroutineScope(Dispatchers.IO)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewmodel = viewModel
        binding.activity = this
        binding.lifecycleOwner = this

        accidentReceiver = AccidentReceiver(viewModel,applicationContext,map_view)

        permissionUtil= PermissionUtil(applicationContext, object:PermissionListener{
            override fun onPermissionGranted() {
                mapViewInit(map_view)
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {}
        })
        permissionUtil.requestPermission()

        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        token = intent.getStringExtra("token").toString()



        navigationView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.logout_item) {
                logout()
            }
            false
        }


        soundSwitch= navigationView.menu.findItem(R.id.sound_alert_item).actionView.findViewById(R.id.drawer_sound) as Switch
        vibrationSwitch = navigationView.menu.findItem(R.id.vibration_alert_item).actionView.findViewById(R.id.drawer_vibrator) as Switch
        soundSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
        vibrationSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
        viewModel.get().observe(this, Observer<Alert> {
            if(it==null){
                ioScope.launch {
                    viewModel.insert(Alert(id = 1,vibration = true,sound = true))
                }
            }else{
                soundSwitch.isChecked=it.sound
                vibrationSwitch.isChecked=it.vibration
            }
        })
    }

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        ioScope.launch{
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


    fun mapViewInit(mapView: MapView){
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
        val acceleration = FloatArray(3)
        var total:Double = 0.0

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2] //중력가속도 계산

        acceleration[0]= event.values[0] - gravity[0]
        acceleration[1]= event.values[1] - gravity[1]
        acceleration[2]= event.values[2] - gravity[2] // 중력을 뺀 가속도.

        total = sqrt(acceleration[0].toDouble().pow(2.0) + acceleration[1].toDouble().pow(2.0) + acceleration[2].toDouble().pow(2.0))

        //
        if(total>2.0*9.8 && token.isNotEmpty() && !accident ) {
            ioScope.launch {
                viewModel.notifyAccident()
                accident=true
            }
        }
    }


    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        removeToken()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    fun settingBtnClicked() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.openDrawer(Gravity.LEFT)
        } else {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(accidentReceiver, IntentFilter ("accident"));
    }

    override fun onPause() {
        super.onPause()
        removeToken()
        accident = false
        unregisterReceiver(accidentReceiver)
    }

    private fun removeToken() {
        ioScope.launch {
            if (token != "") {
                viewModel.deleteLocation(token)
            }
        }
    }

    object GLOBAL {
        var token: String = ""
    }
}
