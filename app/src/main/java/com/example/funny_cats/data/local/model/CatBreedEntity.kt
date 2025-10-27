package com.example.funny_cats.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_breeds")
data class CatBreedEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val origin: String?,
    val temperament: String?,
    val description: String?,
    val wikipediaUrl: String?,
    val imageId: String?,
    val lifeSpan: String?,
    val intelligence: Int?,
    val dogFriendly: Int?,
    val adaptability: Int?,
    val isInFavorites: Boolean = false,
    val isInWatchLater: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)