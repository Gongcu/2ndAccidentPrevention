package com.example.a2ndaccidentprevention

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.a2ndaccidentprevention.retrofit.LocationInfo
import com.example.a2ndaccidentprevention.retrofit.LocationService
import com.example.a2ndaccidentprevention.retrofit.RetrofitAPI
import com.example.a2ndaccidentprevention.room.Alert
import com.example.a2ndaccidentprevention.room.AlertDatabase
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class Repository(application: Application) {
    private val alertDatabase = AlertDatabase.getInstance(application.applicationContext)!!
    private val alertDao = alertDatabase.alertDao()
    private val alert : LiveData<Alert> = alertDao.get()
    private var auth = FirebaseAuth.getInstance()

    private val retrofit = RetrofitAPI.getInstance()
    private val api = retrofit.create(LocationService::class.java)

    fun get(): LiveData<Alert> {
        return alert
    }
    fun update(alert: Alert){
        alertDao.update(alert)
    }
    fun insert(alert: Alert){
        alertDao.insert(alert)
    }
    fun postLocation(locationInfo: LocationInfo){
        api.postLocation(locationInfo)
    }
    fun notifyAccident(locationInfoList: Queue<LocationInfo>){
        api.notifyAccident(locationInfoList).enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("onResponse",response.toString())
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("onFailure",t.toString())
            }
        });
    }
    fun deleteLocation(token: String){
        api.deleteLocation(token)
    }
    fun getAuth():FirebaseAuth{bg
        return auth
    }
    fun getUid():String?{
        return auth.currentUser?.uid.toString()
    }
}