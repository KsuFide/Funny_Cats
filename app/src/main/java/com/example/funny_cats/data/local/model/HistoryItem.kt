package com.example.funny_cats.data.local.model

sealed class HistoryItem {
    data class BreedHistory(
        val breed: CatBreedEntity,
        val viewedAt: Long
    ) : HistoryItem()

    data class ImageHistory(
        val image: CatImage,
        val viewedAt: Long
    ) : HistoryItem()
}