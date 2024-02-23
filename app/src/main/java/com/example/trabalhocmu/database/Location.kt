package com.example.trabalhocmu.database

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class Location(
    @SerializedName("postcode")
    val postcode: String
)

interface NominatimAPI {
    @GET("reverse")
    fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("zoom") zoom: Int = 17,
        @Query("format") format: String = "json"
    ): Call<JsonObject>
}


object  RetrofitHelper {

    val baseUrl = "https://nominatim.openstreetmap.org/"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build()
    }
}

