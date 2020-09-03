package com.example.a2ndaccidentprevention.util

import android.content.Context
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

class PermissionUtil(context: Context) {
    private val context = context
    private var listener: PermissionListener? = null
    private val permissions = arrayOf<String>(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    init {
        listener = object : PermissionListener {
            override fun onPermissionGranted() {}
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {}
        }
    }

    fun requestPermission(){
        TedPermission.with(context)
                .setPermissionListener(listener)
                .setRationaleMessage("앱을 사용하려면 위치정보가 필요합니다. 다음에 나오는 권한을 허용해주세요.")
                .setDeniedMessage("권한을 거부하셨습니다..\n앱을 이용하려면 [설정] > [권한] 에서 권한을 허용하세요.")
                .setPermissions(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .check();
    }
}
