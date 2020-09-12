package com.example.a2ndaccidentprevention.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.example.a2ndaccidentprevention.ViewModel
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import net.daum.mf.map.api.MapView

class AccidentReceiver(private val viewModel: ViewModel, private val activityContext: Context, private val mapView: MapView) :BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!=null){ //filtering when receiver registered
            val accidentLatitude = intent.getDoubleExtra("accidentLatitude",0.0)
            val accidentLongitude = intent.getDoubleExtra("accidentLongitude",0.0)
            val list = intent.getSerializableExtra("list") as ArrayList<LocationInfo>
            val accidentLocation = Location("accidentLocation")
            accidentLocation.latitude=accidentLatitude
            accidentLocation.longitude=accidentLongitude
            if(AlertGenerator.isAccident(viewModel.getBearing(),accidentLocation,list)){
                Log.d("AccidentReceiver","Accident occur")
                val alert = viewModel.get().value
                if(alert!=null){
                    if(alert.sound)
                        AlertGenerator.generateSound(activityContext)
                    if(alert.vibration)
                        AlertGenerator.generateVibration(activityContext)
                }
                AlertGenerator.showMarker(activityContext, viewModel.mapPoint.value!!, mapView )
            }else{
                Log.d("AccidentReceiver","this is not accident")
            }
        }
    }
}