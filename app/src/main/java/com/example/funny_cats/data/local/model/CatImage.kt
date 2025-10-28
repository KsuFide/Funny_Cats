package com.example.funny_cats.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "cat_images")
data class CatImage(
    @PrimaryKey
    @SerializedName("id")
    val id: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    // Добавим поле для избранного
    val isInFavorites: Boolean = false,

    // Добавим поле для временной метки
    val lastUpdated: Long = System.currentTimeMillis()
)