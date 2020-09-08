package com.example.a2ndaccidentprevention.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.util.Log
import com.beust.klaxon.Klaxon
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*
import kotlin.collections.ArrayList


class MyFirebaseMessagingService : FirebaseMessagingService(){
    @SuppressLint("LongLogTag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val accidentLatitude = remoteMessage.data["latitude"]!!.toDouble()
            val accidentLongitude = remoteMessage.data["longitude"]!!.toDouble()
            val locationInfoList = remoteMessage.data["accidentInfo"]
            val list = Collections.unmodifiableList(Klaxon().parseArray<LocationInfo>(locationInfoList.toString()))

            if(applicationInForeground()){
                Log.d(TAG,"send to receiver")
                val intent = Intent("accident")
                intent.putExtra("accidentLatitude",accidentLatitude)
                intent.putExtra("accidentLongitude",accidentLongitude)
                intent.putExtra("list",ArrayList(list))
                this.sendBroadcast(intent)
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

    }

    @SuppressLint("LongLogTag")
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    @SuppressLint("LongLogTag")
    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun applicationInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.runningAppProcesses
        var isActivityFound = false
        if (services[0].processName
                        .equals(packageName, ignoreCase = true) && services[0].importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            isActivityFound = true
        }
        return isActivityFound
    }

    companion object{
        private const val TAG:String = "MyFirebaseMessagingService"
    }
}

