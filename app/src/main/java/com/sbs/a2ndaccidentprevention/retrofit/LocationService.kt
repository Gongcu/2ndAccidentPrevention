package com.sbs.a2ndaccidentprevention.retrofit

import retrofit2.Call
import retrofit2.http.*

interface LocationService {
    @POST("location")
    fun postLocation(@Body locationInfo: LocationInfo): Call<Void>

    @POST("location/accident") //사고 발생 가능성 차량의 행적을 전달.
    fun notifyAccident(@Body accidentInfo: AccidentInfo): Call<Void>

    @HTTP(method = "DELETE", path = "/location", hasBody = true)
    fun deleteLocation(@Body token: String): Call<Void>
}