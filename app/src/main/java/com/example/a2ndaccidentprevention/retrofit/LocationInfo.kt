package com.example.a2ndaccidentprevention.retrofit


import java.io.Serializable
import java.util.*


data class AccidentInfo(
        val accidentLocation:LocationInfo,
        val locationInfoList: Queue<LocationInfo>)

data class LocationInfo(
        val token: String,
        val latitude: Double,
        val longitude: Double,
        var bearing: Float):Serializable

