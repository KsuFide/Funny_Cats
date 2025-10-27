package com.example.funny_cats.data.model

import com.google.gson.annotations.SerializedName

data class CatImage(
    @SerializedName("id")
    val id: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("breeds")
    val breeds: List<CatBreed>?
)