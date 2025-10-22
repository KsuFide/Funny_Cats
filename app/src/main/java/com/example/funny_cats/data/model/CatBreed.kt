package com.example.funny_cats.data.model

import com.google.gson.annotations.SerializedName

data class CatBreed(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("temperament")
    val temperament: String,

    @SerializedName("origin")
    val origin: String,

    @SerializedName("life_span")
    val lifeSpan: String,

    @SerializedName("adaptability")
    val adaptability: Int,

    @SerializedName("intelligence")
    val intelligence: Int,

    @SerializedName("dog_friendly")
    val dogFriendly: Int,

    @SerializedName("wikipedia_url")
    val wikipediaUrl: String?,

    @SerializedName("reference_image_id")
    val referenceImageId: String?
)