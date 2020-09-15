package com.example.a2ndaccidentprevention.util

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.core.app.ActivityCompat
import com.example.a2ndaccidentprevention.ViewModel
import com.example.a2ndaccidentprevention.view.LoginActivity
import com.example.a2ndaccidentprevention.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapView

object Util {
     fun logout(activity: Activity){
         FirebaseAuth.getInstance().signOut()
         val intent = Intent(activity, LoginActivity::class.java)
         activity.startActivity(intent)
         activity.finish()
    }

     fun removeToken(viewModel: ViewModel) {
        CoroutineScope(Dispatchers.IO).launch {
            if (MainActivity.GLOBAL.token != "") {
                viewModel.deleteLocation(MainActivity.GLOBAL.token)
            }
        }
    }
    fun mapViewInit(viewModel: ViewModel, map_view:MapView?){
        if(map_view!=null) {
            map_view.setZoomLevel(2, true)
            map_view.zoomIn(true)
            map_view.zoomOut(true)
            map_view.isHDMapTileEnabled = false //고해상도 지도 사용 안함
            map_view.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
            map_view.setCurrentLocationEventListener(viewModel)
        }
    }
    fun requestPermission(application: Application, listener: PermissionListener){
        TedPermission.with(application)
                .setPermissionListener(listener)
                .setRationaleMessage("앱을 사용하려면 위치정보가 필요합니다. 다음에 나오는 권한을 허용해주세요.")
                .setDeniedMessage("권한을 거부하셨습니다..\n앱을 이용하려면 [설정] > [권한] 에서 권한을 허용하세요.")
                .setPermissions(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .check()
    }
}