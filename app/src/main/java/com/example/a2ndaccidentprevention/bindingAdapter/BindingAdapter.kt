package com.example.a2ndaccidentprevention.bindingAdapter

import android.content.pm.PackageManager
import android.widget.CompoundButton
import android.widget.Switch
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.example.a2ndaccidentprevention.ViewModel
import com.example.a2ndaccidentprevention.room.Alert
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.util.jar.Manifest

object BindingAdapter {
    @BindingAdapter("mapViewUpdate")
    @JvmStatic
    fun updateView(mapView: MapView, mapPoint: MapPoint?) {
        if (mapPoint != null) {
            mapView.setMapCenterPoint(mapPoint, true)
            mapView.setCurrentLocationRadius(250) //m단위  250이란 값이 실제 지도에서 1km정도에 해당됨
            mapView.setCurrentLocationRadiusFillColor(android.graphics.Color.argb(10, 255, 0, 0))
            mapView.setCurrentLocationRadiusStrokeColor(android.graphics.Color.argb(100, 255, 0, 0))
        }
    }
    /*
    /*Binding: 전달된 값으로 UI를 변화*/
    @InverseBindingAdapter(attribute = "android:checked")
    @JvmStatic
    fun setChecked(switch: Switch,checked:Boolean) {
        switch.isChecked=checked
    }

    /*InverseBinding: UI의 변화를 전달*/
    @InverseBindingAdapter(attribute = "android:checked", event="switchEvent")
    @JvmStatic
    fun getChecked(switch: Switch):Boolean {
        return switch.isChecked
    }

    /*UI의 변화를 알리기 위한 리스너*/
    //UI에 등록한 리스너에서 변화가 감지되면 이벤트가 switchEvent인 InverseBindingAdapter가 호출된다.
    @BindingAdapter("switchEvent")
    @JvmStatic
    fun setSwitchEvent(switch:Switch, listener: InverseBindingListener){
        switch.setOnCheckedChangeListener( object:CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                listener.onChange()
            }
        })
    }*/
}