package com.sbs.a2ndaccidentprevention.listener

import android.widget.CompoundButton
import com.sbs.a2ndaccidentprevention.R
import com.sbs.a2ndaccidentprevention.ViewModel
import com.sbs.a2ndaccidentprevention.util.Util
import com.sbs.a2ndaccidentprevention.view.MainActivity
import com.google.android.material.navigation.NavigationView
import com.gun0912.tedpermission.PermissionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapView

class ListenerPackage(private val viewModel: ViewModel, private val activity: MainActivity) {
    private val ioScope = CoroutineScope(Dispatchers.IO)

     val onItemSelectedListener = NavigationView.OnNavigationItemSelectedListener{
        if (it.itemId == R.id.logout_item) {
            Util.logout(activity)
            Util.removeToken(viewModel)
        }
        false
    }

     val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
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

    inner class LocationPermission(private val map_view:MapView) : PermissionListener{
        override fun onPermissionGranted() {
            Util.mapViewInit(viewModel, map_view)
        }
        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
        }
    }
}