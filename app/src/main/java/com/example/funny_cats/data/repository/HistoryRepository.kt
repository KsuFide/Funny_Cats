package com.example.funny_cats.data.repository

import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.ui.history.CombinedHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val database: CatDatabase
) {

    private val breedDao = database.catBreedDao()
    private val imageDao = database.catImageDao()

    // Получаем объединенную историю (породы + изображения)
    fun getCombinedHistory(): Flow<List<CombinedHistoryItem>> {
        val breedsFlow = breedDao.getAllBreeds().map { breeds ->
            breeds.filter { it.lastViewed > 0 }
                .map { CombinedHistoryItem.BreedItem(it, it.lastViewed) }
        }

        val imagesFlow = imageDao.getAllImages().map { images ->
            images.filter { it.lastUpdated > 0 }
                .map { CombinedHistoryItem.ImageItem(it, it.lastUpdated) }
        }

        return breedsFlow.combine(imagesFlow) { breeds, images ->
            (breeds + images).sortedByDescending {
                when (it) {
                    is CombinedHistoryItem.BreedItem -> it.viewedAt
                    is CombinedHistoryItem.ImageItem -> it.viewedAt
                }
            }
        }
    }

    // Получаем только историю пород
    fun getBreedHistory(): Flow<List<CatBreedEntity>> {
        return breedDao.getAllBreeds().map { breeds ->
            breeds.filter { it.lastViewed > 0 }
                .sortedByDescending { it.lastViewed }
        }
    }

    // Получаем только историю изображений
    fun getImageHistory(): Flow<List<CatImage>> {
        return imageDao.getAllImages().map { images ->
            images.filter { it.lastUpdated > 0 }
                .sortedByDescending { it.lastUpdated }
        }
    }

    // Очищаем всю историю
    suspend fun clearAllHistory() {
        breedDao.clearViewHistory()
        imageDao.clearViewHistory()
    }

    // Очищаем историю пород
    suspend fun clearBreedHistory() {
        breedDao.clearViewHistory()
    }

    // Очищаем историю изображений
    suspend fun clearImageHistory() {
        imageDao.clearViewHistory()
    }
}