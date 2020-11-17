package com.sbs.a2ndaccidentprevention.view


import android.content.Context
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Switch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.sbs.a2ndaccidentprevention.R
import com.sbs.a2ndaccidentprevention.ViewModel
import com.sbs.a2ndaccidentprevention.view.MainActivity.GLOBAL.token
import com.sbs.a2ndaccidentprevention.databinding.ActivityMainBinding
import com.sbs.a2ndaccidentprevention.listener.ListenerPackage
import com.sbs.a2ndaccidentprevention.room.Alert
import com.sbs.a2ndaccidentprevention.util.AccidentReceiver
import com.sbs.a2ndaccidentprevention.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(),SensorEventListener {
    private var accident = false
    private val viewModel: ViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val accidentReceiver : AccidentReceiver by lazy{
        AccidentReceiver(viewModel,applicationContext,map_view)
    }

    private lateinit var sensorManager:SensorManager
    private lateinit var sensor: Sensor

    private val soundSwitch: Switch by lazy{
        navigationView.menu.findItem(R.id.sound_alert_item).actionView.findViewById(R.id.drawer_sound) as Switch
    }
    private val vibrationSwitch: Switch by lazy{
        navigationView.menu.findItem(R.id.vibration_alert_item).actionView.findViewById(R.id.drawer_vibrator) as Switch
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val listenerPackage : ListenerPackage by lazy{
        ListenerPackage(viewModel,this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewmodel = viewModel
        binding.activity = this
        binding.lifecycleOwner = this

        Util.requestPermission(application,listenerPackage.LocationPermission(map_view))

        token = intent.getStringExtra("token").toString()


        viewModel.get().observe(this, Observer<Alert> {
            soundSwitch.isChecked=it.sound
            vibrationSwitch.isChecked=it.vibration
        })

        viewModel.getSpeed().observe(this, Observer {
            speed_text_view.text = it.toBigDecimal().toPlainString().substring(0,3)
        })

        //Accelerometer
        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL)


        navigationView.setNavigationItemSelectedListener(listenerPackage.onItemSelectedListener)
        soundSwitch.setOnCheckedChangeListener(listenerPackage.onCheckedChangeListener)
        vibrationSwitch.setOnCheckedChangeListener(listenerPackage.onCheckedChangeListener)
    }

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
                viewModel.notifyAccident()
                accident=true
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //unused
    }


    fun settingBtnClicked(){
        if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.openDrawer(GravityCompat.END)
        } else {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(accidentReceiver, IntentFilter (AccidentReceiver.ACCIDENT))
    }

    override fun onPause() {
        super.onPause()
        Util.removeToken(viewModel)
        unregisterReceiver(accidentReceiver)
    }

    object GLOBAL {
        var token: String = ""
    }
}
