package com.example.a2ndaccidentprevention.retrofit

import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface LocationService {
    @POST("location")
    fun postLocation(@Body locationInfo: LocationInfo): Call<Void>

    @GET("location/accident") //사고 발생 가능성 차량의 행적을 전달.
    fun notifyAccident(@Body locationInfoList: Queue<LocationInfo>): Call<Void>

    @HTTP(method = "DELETE", path = "/ocation", hasBody = true)
    fun deleteLocation(@Body token: String): Call<Void>
}