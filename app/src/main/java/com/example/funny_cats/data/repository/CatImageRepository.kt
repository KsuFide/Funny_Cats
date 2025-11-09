package com.example.funny_cats.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatImage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CatImageRepository @Inject constructor(
    private val database: CatDatabase
) {

    private val dao = database.catImageDao()

    // Пагинация для всех изображений
    fun getImagesPaging(): Flow<PagingData<CatImage>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                maxSize = 100
            ),
            pagingSourceFactory = {
                dao.getPagingSource()
            }
        ).flow
    }

    // Пагинация для избранных изображений
    fun getFavoriteImagesPaging(): Flow<PagingData<CatImage>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                maxSize = 100
            ),
            pagingSourceFactory = {
                dao.getFavoriteImagesPagingSource()
            }
        ).flow
    }

    // Загрузка случайных изображений из API
    suspend fun refreshRandomImages(limit: Int = 10) {
        try {
            val imagesFromApi = RetrofitInstance.api.getRandomCats(limit)
            // Добавляем временную метку для сортировки
            val imagesWithTimestamp = imagesFromApi.map { it.copy(lastUpdated = System.currentTimeMillis()) }
            dao.insertAll(imagesWithTimestamp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Загрузка изображений породы
    suspend fun loadBreedImages(breedId: String, limit: Int = 8): List<CatImage> {
        return try {
            val imagesFromApi = RetrofitInstance.api.getBreedImages(breedId, limit)

            // Для каждого изображения проверяем состояние в базе
            val imagesWithFavoriteStatus = mutableListOf<CatImage>()
            for (apiImage in imagesFromApi) {
                val imageFromDb = dao.getImageById(apiImage.id)
                val isFavorite = imageFromDb?.isInFavorites ?: false
                // Сохраняем изображение в базу с правильным состоянием избранного
                val imageToSave = apiImage.copy(
                    isInFavorites = isFavorite,
                    lastUpdated = System.currentTimeMillis()
                )
                dao.insertAll(listOf(imageToSave))
                imagesWithFavoriteStatus.add(imageToSave)
            }

            imagesWithFavoriteStatus
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Обновление статуса избранного
    suspend fun toggleFavorite(imageId: String, isFavorite: Boolean) {
        dao.updateFavoriteStatus(imageId, isFavorite)
    }

    // Получение изображения по ID
    suspend fun getImageById(imageId: String): CatImage? {
        return dao.getImageById(imageId)
    }

    suspend fun updateViewTime(imageId: String) {
        dao.updateViewTime(imageId, System.currentTimeMillis())
    }

    // Очистка кэша
    suspend fun clearCache() {
        dao.clearAll()
    }
}