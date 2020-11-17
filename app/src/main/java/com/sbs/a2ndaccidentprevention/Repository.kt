package com.sbs.a2ndaccidentprevention

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.sbs.a2ndaccidentprevention.retrofit.AccidentInfo
import com.sbs.a2ndaccidentprevention.retrofit.LocationInfo
import com.sbs.a2ndaccidentprevention.retrofit.LocationService
import com.sbs.a2ndaccidentprevention.retrofit.RetrofitAPI
import com.sbs.a2ndaccidentprevention.room.Alert
import com.sbs.a2ndaccidentprevention.room.AlertDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class Repository(application: Application) {
    private val alertDatabase = AlertDatabase.getInstance(application.applicationContext)!!
    private val alertDao = alertDatabase.alertDao()
    private val alert : LiveData<Alert> = alertDao.get()
    private val retrofit = RetrofitAPI.getInstance()
    private val api = retrofit.create(LocationService::class.java)

    fun get(): LiveData<Alert> {
        return alert
    }
    suspend fun update(alert: Alert){
        alertDao.update(alert)
    }
    suspend fun updateVibration(status: Boolean){
        alertDao.updateVibration(status)
    }
    suspend fun updateSound(status: Boolean){
        alertDao.updateSound(status)
    }
    suspend fun insert(alert: Alert){
        alertDao.insert(alert)
    }
    fun postLocation(locationInfo: LocationInfo){
        api.postLocation(locationInfo).enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("post:onResponse",response.toString())
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("post:onFailure",t.toString())
            }
        });
    }
    fun notifyAccident(accidentLocation: LocationInfo, locationInfoList: Queue<LocationInfo>){
        api.notifyAccident(accidentInfo = AccidentInfo(accidentLocation,locationInfoList)).enqueue(object: Callback<Void>{
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
}