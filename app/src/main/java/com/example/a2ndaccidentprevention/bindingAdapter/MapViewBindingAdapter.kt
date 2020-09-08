package com.example.a2ndaccidentprevention.bindingAdapter

import androidx.databinding.BindingAdapter
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

object BindingAdapter {
    @BindingAdapter("mapViewInit")
    @JvmStatic
    fun initView(mapView: MapView, mapPoint:MapPoint?){
        if(mapPoint!=null) {
            mapView.setMapCenterPoint(mapPoint, true)
            mapView.setCurrentLocationRadius(250) //m단위  250이란 값이 실제 지도에서 1km정도에 해당됨
            mapView.setCurrentLocationRadiusFillColor(android.graphics.Color.argb(10, 255, 0, 0))
            mapView.setCurrentLocationRadiusStrokeColor(android.graphics.Color.argb(100, 255, 0, 0))
        }
    }
}