package com.example.a2ndaccidentprevention

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.a2ndaccidentprevention.retrofit.Location
import com.example.a2ndaccidentprevention.retrofit.LocationService
import com.example.a2ndaccidentprevention.retrofit.RetrofitAPI
import com.example.a2ndaccidentprevention.room.Alert
import com.example.a2ndaccidentprevention.room.AlertDatabase
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
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
    fun postLocation(location: Location){
        api.postLocation(location).enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("onResponse",response.toString())
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("onFailure",t.toString())
            }
        });
    }
    fun notifyAccident(locationList: Queue<Location>){
        api.notifyAccident(locationList).enqueue(object: Callback<Void>{
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
    fun getAuth():FirebaseAuth{
        return auth
    }
    fun getUid():String?{
        return auth.currentUser?.uid.toString()
    }
}