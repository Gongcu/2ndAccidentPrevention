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
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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
import com.google.android.material.navigation.NavigationView
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


class MainActivity : AppCompatActivity() {
    private val viewModel: ViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var accidentReceiver : AccidentReceiver
    private lateinit var permissionUtil: PermissionUtil

    private val soundSwitch: Switch by lazy{
       navigationView.menu.findItem(R.id.sound_alert_item).actionView.findViewById(R.id.drawer_sound) as Switch
    }
    private val vibrationSwitch: Switch by lazy{
        navigationView.menu.findItem(R.id.vibration_alert_item).actionView.findViewById(R.id.drawer_vibrator) as Switch
    }

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
                mapViewInit()
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {}
        })
        permissionUtil.requestPermission()


        token = intent.getStringExtra("token").toString()


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

        viewModel.acceleratorLiveData.observe(this,Observer{
            Log.d("accelerator sensor","onWork")
        })

        navigationView.setNavigationItemSelectedListener(onItemSelectedListener)
        soundSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
        vibrationSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
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

    private val onItemSelectedListener = NavigationView.OnNavigationItemSelectedListener{
        if (it.itemId == R.id.logout_item) {
            logout()
        }
        false
    }

    fun settingBtnClicked(){
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.openDrawer(GravityCompat.START)
        } else {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private fun mapViewInit(){
        if(map_view!=null) {
            map_view.setZoomLevel(2, true)
            map_view.zoomIn(true)
            map_view.zoomOut(true)
            map_view.isHDMapTileEnabled = false //고해상도 지도 사용 안함
            map_view.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
            map_view.setCurrentLocationEventListener(viewModel)
        }
    }

    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        removeToken()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(accidentReceiver, IntentFilter ("accident"));
    }

    override fun onPause() {
        super.onPause()
        removeToken()
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
