package com.example.a2ndaccidentprevention.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitAPI{
    private var instance:Retrofit? = null
    fun getInstance() : Retrofit{
        if(instance==null){
            instance = Retrofit.Builder()
                    .baseUrl("http://49.50.174.166:3000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
        return instance!!
    }
}