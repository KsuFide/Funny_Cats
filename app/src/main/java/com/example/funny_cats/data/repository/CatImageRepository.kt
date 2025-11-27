package com.example.funny_cats.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.data.paging.RandomCatsPagingSource
import com.example.funny_cats.util.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CatImageRepository @Inject constructor(
    private val database: CatDatabase
) {

    private val dao = database.catImageDao()

    fun getImagesPaging(): Flow<PagingData<CatImage>> {
        Logger.d("Getting images paging flow")
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
            Logger.d("Refreshing random images, limit: $limit")
            val imagesFromApi = RetrofitInstance.api.getRandomCats(limit)
            val imagesWithTimestamp = imagesFromApi.map { it.copy(lastUpdated = System.currentTimeMillis()) }
            dao.insertAll(imagesWithTimestamp)
            Logger.d("Successfully refreshed $limit random images")
        } catch (e: Exception) {
            Logger.e("Failed to refresh random images: ${e.message}", e)
        }
    }

    suspend fun loadBreedImages(breedId: String, limit: Int = 8): List<CatImage> {
        return try {
            Logger.d("Loading breed images for breed: $breedId, limit: $limit")
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

            Logger.d("Successfully loaded ${imagesWithFavoriteStatus.size} breed images")
            imagesWithFavoriteStatus
        } catch (e: Exception) {
            Logger.e("Failed to load breed images for breed $breedId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun toggleFavorite(imageId: String, isFavorite: Boolean) {
        try {
            dao.updateFavoriteStatus(imageId, isFavorite)
            Logger.d("Updated favorite status for image $imageId to $isFavorite")
        } catch (e: Exception) {
            Logger.e("Failed to toggle favorite for image $imageId: ${e.message}", e)
            throw e
        }
    }

    suspend fun getImageById(imageId: String): CatImage? {
        return try {
            dao.getImageById(imageId)
        } catch (e: Exception) {
            Logger.e("Failed to get image by id $imageId: ${e.message}", e)
            null
        }
    }

    suspend fun updateViewTime(imageId: String) {
        try {
            dao.updateViewTime(imageId, System.currentTimeMillis())
        } catch (e: Exception) {
            Logger.e("Failed to update view time for image $imageId: ${e.message}", e)
        }
    }

    suspend fun clearCache() {
        try {
            dao.clearAll()
            Logger.d("Successfully cleared image cache")
        } catch (e: Exception) {
            Logger.e("Failed to clear cache: ${e.message}", e)
        }
    }
}