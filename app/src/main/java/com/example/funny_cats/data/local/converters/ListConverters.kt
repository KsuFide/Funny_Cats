package com.example.funny_cats.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListConverters {

    @TypeConverter
    fun fromString(value: String?): List<Int> {
        return if (value.isNullOrEmpty()) {
            emptyList()
        } else {
            val listType = object : TypeToken<List<Int>>() {}.type
            Gson().fromJson(value, listType)
        }
    }

    @TypeConverter
    fun toString(list: List<Int>?): String {
        return if (list.isNullOrEmpty()) {
            ""
        } else {
            Gson().toJson(list)
        }
    }
}