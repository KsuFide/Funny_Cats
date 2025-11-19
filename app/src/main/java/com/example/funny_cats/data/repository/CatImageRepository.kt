package com.example.funny_cats.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.data.paging.RandomCatsPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CatImageRepository @Inject constructor(
    private val database: CatDatabase
) {

    private val dao = database.catImageDao()

    // Пагинация для всех изображений - ИСПОЛЬЗУЕМ PAGING SOURCE КОТОРЫЙ РАБОТАЕТ С БАЗОЙ
    fun getImagesPaging(): Flow<PagingData<CatImage>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                maxSize = 100
            ),
            pagingSourceFactory = {
                RandomCatsPagingSource(database)
            }
        ).flow
    }


    suspend fun refreshRandomImages(limit: Int = 10) {
        try {
            val imagesFromApi = RetrofitInstance.api.getRandomCats(limit)
            val imagesWithTimestamp = imagesFromApi.map { it.copy(lastUpdated = System.currentTimeMillis()) }
            dao.insertAll(imagesWithTimestamp)
            Log.d("CatImageRepository", "Successfully refreshed $limit random images")
        } catch (e: Exception) {
            Log.e("CatImageRepository", "Failed to refresh random images: ${e.message}", e)
            // Не падаем, просто логируем ошибку
        }
    }

    suspend fun loadBreedImages(breedId: String, limit: Int = 8): List<CatImage> {
        return try {
            val imagesFromApi = RetrofitInstance.api.getBreedImages(breedId, limit)

            val imagesWithFavoriteStatus = mutableListOf<CatImage>()
            for (apiImage in imagesFromApi) {
                val imageFromDb = dao.getImageById(apiImage.id)
                val isFavorite = imageFromDb?.isInFavorites ?: false
                val imageToSave = apiImage.copy(
                    isInFavorites = isFavorite,
                    lastUpdated = System.currentTimeMillis()
                )
                dao.insertAll(listOf(imageToSave))
                imagesWithFavoriteStatus.add(imageToSave)
            }

            Log.d("CatImageRepository", "Successfully loaded ${imagesWithFavoriteStatus.size} breed images")
            imagesWithFavoriteStatus
        } catch (e: Exception) {
            Log.e("CatImageRepository", "Failed to load breed images for breed $breedId: ${e.message}", e)
            emptyList() // Возвращаем пустой список вместо падения
        }
    }

    suspend fun toggleFavorite(imageId: String, isFavorite: Boolean) {
        try {
            dao.updateFavoriteStatus(imageId, isFavorite)
            Log.d("CatImageRepository", "Updated favorite status for image $imageId to $isFavorite")
        } catch (e: Exception) {
            Log.e("CatImageRepository", "Failed to toggle favorite for image $imageId: ${e.message}", e)
            throw e // Пробрасываем исключение, так как это критическая операция
        }
    }

    suspend fun getImageById(imageId: String): CatImage? {
        return try {
            dao.getImageById(imageId)
        } catch (e: Exception) {
            Log.e("CatImageRepository", "Failed to get image by id $imageId: ${e.message}", e)
            null
        }
    }

    suspend fun updateViewTime(imageId: String) {
        try {
            dao.updateViewTime(imageId, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e("CatImageRepository", "Failed to update view time for image $imageId: ${e.message}", e)
        }
    }

    suspend fun clearCache() {
        try {
            dao.clearAll()
            Log.d("CatImageRepository", "Successfully cleared image cache")
        } catch (e: Exception) {
            Log.e("CatImageRepository", "Failed to clear cache: ${e.message}", e)
        }
    }
}