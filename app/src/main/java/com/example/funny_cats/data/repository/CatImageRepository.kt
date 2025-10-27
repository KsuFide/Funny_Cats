package com.example.funny_cats.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatImage
import kotlinx.coroutines.flow.Flow

class CatImageRepository(private val database: CatDatabase) {

    private val dao = database.catImageDao()

    // Пагинация для всех изображений
    fun getImagesPaging(): Flow<PagingData<CatImage>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10, // Загружаем по 10 изображений за раз
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

    // Обновление статуса избранного
    suspend fun toggleFavorite(imageId: String, isFavorite: Boolean) {
        dao.updateFavoriteStatus(imageId, isFavorite)
    }

    // Очистка кэша
    suspend fun clearCache() {
        dao.clearAll()
    }
}