package com.sbs.a2ndaccidentprevention.util
import android.content.Context
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.sbs.a2ndaccidentprevention.R
import com.sbs.a2ndaccidentprevention.retrofit.LocationInfo
import com.muddzdev.styleabletoast.StyleableToast
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


object AlertGenerator{

    fun isAccident(currentBearing: Float,accidentLocation:Location,items:List<LocationInfo>): Boolean {
        var minBearing = 360.0f
        var maxBearing = 0.0f
        //get Min, Max Bearing
        for (i in items.indices) {
            if (minBearing > items[i].bearing && items[i].bearing != 0.0f) minBearing = items[i].bearing
            if (maxBearing < items[i].bearing && items[i].bearing != 0.0f) maxBearing = items[i].bearing

            //below code is test code when device is stop state
            //if (minBearing > items[i].bearing ) minBearing = items[i].bearing
            //if (maxBearing < items[i].bearing ) maxBearing = items[i].bearing
        }


        for (i in items.indices) {
            //사고 발생 가능성 차량의 행적
            val trackingLocation = Location(i.toString())
            trackingLocation.longitude=items[i].longitude
            trackingLocation.latitude=items[i].latitude
            val distance: Float = accidentLocation.distanceTo(trackingLocation)
            if (distance < 135.0 && currentBearing >= minBearing && currentBearing <= maxBearing) {
                return true
            } else {
                continue
            }
        }
        return false
    }

    fun showMarker(context:Context,mapPoint:MapPoint, mapView: MapView){
        val customMarker = MapPOIItem()
        customMarker.itemName = "급감속 발생 위치"
        customMarker.tag = 1
        customMarker.mapPoint = mapPoint
        customMarker.markerType = MapPOIItem.MarkerType.CustomImage // 마커타입을 커스텀 마커로 지정.
        customMarker.customImageResourceId = R.drawable.accident_spot // 마커 이미지.
        customMarker.isCustomImageAutoscale = false // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
        customMarker.setCustomImageAnchor(0.5f, 0.5f) // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
        Handler(Looper.getMainLooper()).postDelayed(Runnable
                { StyleableToast.makeText(context, "근방에 급감속 발생", Toast.LENGTH_LONG, R.style.mytoast).show() }
                , 0)
        mapView.addPOIItem(customMarker)
    }

    fun generateSound(context:Context){
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.alert)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) as Int, AudioManager.FLAG_PLAY_SOUND)
        mediaPlayer.start()
        Handler(Looper.getMainLooper()).postDelayed(Runnable { audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND) }, 4000)
    }

    fun generateVibration(context:Context){
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.alert)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) as Int, AudioManager.FLAG_PLAY_SOUND)
        mediaPlayer.start()
        Handler(Looper.getMainLooper()).postDelayed(Runnable { audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND) }, 4000)
    }
}