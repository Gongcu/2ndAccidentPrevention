package com.example.a2ndaccidentprevention

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.a2ndaccidentprevention.util.PermissionUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), MapView.CurrentLocationEventListener, SensorEventListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var token: String
    private lateinit var uid: String
    val viewModel: ViewModel by viewModels()

    //private val sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
    //private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    private val gravity = FloatArray(3)
    private var total:Double = 0.0

    private lateinit var permissionUtil: PermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionUtil= PermissionUtil(applicationContext)
        permissionUtil.requestPermission()

        auth = FirebaseAuth.getInstance()
        token = intent.getStringExtra("token").toString()
        uid = intent.getStringExtra("uid").toString()



        navigationView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.logout_item) {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                false
            }
            false
        }
    }

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
        //
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
        //
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {}

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {}



    fun RemoveToken() {/*
        //어플이 종료시 위치를 null 혹은 없는걸로 표시하여 알림이 수신되지 않게 한다.
        // null은 안됨! 0.0//0.0 -> 바다 한가운데!!
        val insertLocationData = InsertLocationData(this)
        if (token != null) {
            insertLocationData.execute("http://" + EXTERNAL_IP_ADDRESS.toString() + "/logout.php", 0.0.toString(), 0.0.toString(), token)
            Log.d("token:", token)
        }*/
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

        /*
        if (total > 2.0 * 9.8 && token != null && (accidentLongitude !== latestLocation.getLongitude() || accidentLatitude !== latestLocation.getLatitude())) { //사고 위치가 바뀌고 중력가속도 기준치 이상
            Log.d("accident", "occur")
            accidentLatitude = latestLocation.getLatitude()
            accidentLongitude = latestLocation.getLongitude()
            val notifyAccident = NotifyAccident(applicationContext)
            notifyAccident.setQueue(queue)
            notifyAccident.execute("http://" + EXTERNAL_IP_ADDRESS.toString() + "/accident.php", java.lang.String.valueOf(accidentLatitude), java.lang.String.valueOf(accidentLongitude), "accident", token)
        }*/
    }

    override fun onPause() {
        super.onPause()
        RemoveToken()
    }
}
