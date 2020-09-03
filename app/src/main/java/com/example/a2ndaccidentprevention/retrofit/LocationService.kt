package com.example.a2ndaccidentprevention.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.*

interface LocationService {
    @GET
    fun getLocation():Call<List<Location>>

    @POST
    fun postLocation(@Body location: Location): Call<Void>

    @POST //사고 발생 가능성 차량의 행적을 전달.
    fun notifyAccident(@Body locationList: Queue<Location>): Call<Void>

    @DELETE
    fun deleteLocation(@Body token: String): Call<Void>
}