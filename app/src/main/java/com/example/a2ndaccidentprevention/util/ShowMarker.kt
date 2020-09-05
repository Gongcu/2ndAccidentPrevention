package com.example.a2ndaccidentprevention.util
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint


class ShowMarker(val latestLocation:Location, val currentBearing: Float){

    fun showMarker(mapPoint: MapPoint?, items: ArrayList<LocationInfo>) {
        var minBearing = 360.0
        var maxBearing = 0.0

        // 같은 장소에서 테스트 시 아래 max,min 구하는 코드를 적용시 문제 있을 수 있음: 같은 장소 테스트시에는 가급적 주석처리.
        for (i in 0 until items.size) {
            if (minBearing > items[i].bearing && items[i].bearing != 0.0) minBearing = items[i].bearing
            if (maxBearing < items[i].bearing && items[i].bearing != 0.0) maxBearing = items[i].bearing
        }
        // End Of Max, Min Algorithm
        for (i in 0 until items.size) {
            val subCircle = Location(i.toString())
            subCircle.longitude=items[i].longitude
            subCircle.latitude=items[i].latitude
            //사고차량이 커피랑 도서관 앞을 95도 방향으로 지난다고 가정
            val distance: Float = latestLocation.distanceTo(subCircle)
            if (distance < 135.0 && currentBearing >= minBearing && currentBearing <= maxBearing) {
                Log.e("currentBearing", "currentBearingcurrentBearingcurrentBearing")
                /*
                val customMarker = MapPOIItem()
                customMarker.itemName = "급감속 발생 위치"
                customMarker.tag = 1
                customMarker.mapPoint = mapPoint
                customMarker.markerType = MapPOIItem.MarkerType.CustomImage // 마커타입을 커스텀 마커로 지정.
                customMarker.customImageResourceId = R.drawable.accident_spot // 마커 이미지.
                customMarker.isCustomImageAutoscale = false // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                customMarker.setCustomImageAnchor(0.5f, 0.5f) // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                mHandler.postDelayed(Runnable { StyleableToast.makeText(getApplicationContext(), "근방에 급감속 발생", Toast.LENGTH_LONG, R.style.mytoast).show() }, 0)
                mapView.addPOIItem(customMarker)
                if (sound_check === 1) { // 소리알림이 켜져 있으면,
                    val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.alert)
                    CurrentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    audioManager.setStreamVolume(audioManager.STREAM_MUSIC,
                            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) as Int, AudioManager.FLAG_PLAY_SOUND)
                    mediaPlayer.start()
                    mHandler.postDelayed(Runnable { audioManager.setStreamVolume(audioManager.STREAM_MUSIC, CurrentVolume, AudioManager.FLAG_PLAY_SOUND) }, 4000)
                }
                if (vibrator_check === 1) {
                    val pattern = longArrayOf(100, 300, 100, 700, 300, 500) // miliSecond
                    //           대기,진동,대기,진동,....
                    // 짝수 인덱스 : 대기시간
                    // 홀수 인덱스 : 진동시간
                    vibrator.vibrate(pattern,  // 진동 패턴을 배열로
                            -1) // 반복 인덱스
                    // 0 : 무한반복, -1: 반복없음,
                }
                */
                break
            } else {
                Log.e("currentBearing", currentBearing.toString() + "")
                Log.e("latestLocation", latestLocation.getLatitude().toString() + "," + latestLocation.getLongitude() + "")
                Log.e("subcircle", subCircle.latitude.toString() + "," + subCircle.longitude + "")
                Log.e("distance", latestLocation.distanceTo(subCircle).toString() + "")
                continue
            }
        }
    }
}