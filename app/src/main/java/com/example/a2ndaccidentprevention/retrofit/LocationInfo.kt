package com.example.a2ndaccidentprevention.retrofit

data class LocationInfo(
        val token: String,
        val latitude: Double,
        val longitude: Double,
        var bearing: Double)