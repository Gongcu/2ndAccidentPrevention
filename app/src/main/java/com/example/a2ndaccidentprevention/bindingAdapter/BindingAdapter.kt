package com.example.a2ndaccidentprevention.bindingAdapter

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.a2ndaccidentprevention.ViewModel
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
}