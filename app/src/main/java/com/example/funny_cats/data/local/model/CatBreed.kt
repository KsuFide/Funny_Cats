package com.example.funny_cats.data.local.model

import com.google.gson.annotations.SerializedName

data class CatBreed(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("life_span")
    val lifeSpan: String?,

    @SerializedName("origin")
    val origin: String?,

    @SerializedName("temperament")
    val temperament: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("wikipedia_url")
    val wikipediaUrl: String?,

    @SerializedName("intelligence")
    val intelligence: Int?,

    @SerializedName("dog_friendly")
    val dogFriendly: Int?,

    @SerializedName("adaptability")
    val adaptability: Int?,

    @SerializedName("reference_image_id")
    val imageId: String?
) {
    fun getImageUrl(): String {
        return if (!imageId.isNullOrEmpty()) {
            "https://cdn2.thecatapi.com/images/$imageId.jpg"
        } else {
            ""
        }
    }
}