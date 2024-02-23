package com.example.trabalhocmu.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromListToString(list: List<String>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringToList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntListToString(list: List<Int>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringToIntList(value: String?): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }
}