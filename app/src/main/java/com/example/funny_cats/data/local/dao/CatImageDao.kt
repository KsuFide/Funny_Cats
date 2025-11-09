package com.example.funny_cats.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.funny_cats.data.local.model.CatImage
import kotlinx.coroutines.flow.Flow

@Dao
interface CatImageDao {

    // Для пагинации
    @Query("SELECT * FROM cat_images ORDER BY lastUpdated DESC")
    fun getPagingSource(): PagingSource<Int, CatImage>

    // Для избранных изображений
    @Query("SELECT * FROM cat_images WHERE isInFavorites = 1 ORDER BY lastUpdated DESC")
    fun getFavoriteImagesPagingSource(): PagingSource<Int, CatImage>

    // Обычные методы
    @Query("SELECT * FROM cat_images ORDER BY lastUpdated DESC")
    fun getAllImages(): Flow<List<CatImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<CatImage>)

    @Update
    suspend fun updateImage(image: CatImage)

    @Query("UPDATE cat_images SET isInFavorites = :isFavorite WHERE id = :imageId")
    suspend fun updateFavoriteStatus(imageId: String, isFavorite: Boolean)

    @Query("DELETE FROM cat_images")
    suspend fun clearAll()

    @Query("SELECT * FROM cat_images WHERE id = :imageId")
    suspend fun getImageById(imageId: String): CatImage?

    // Для истории просмотров
    @Query("SELECT * FROM cat_images WHERE lastUpdated > 0 ORDER BY lastUpdated DESC")
    fun getHistoryPagingSource(): PagingSource<Int, CatImage>

    // Обновляем время просмотра изображения - ЭТОТ МЕТОД ДОЛЖЕН СУЩЕСТВОВАТЬ
    @Query("UPDATE cat_images SET lastUpdated = :timestamp WHERE id = :imageId")
    suspend fun updateViewTime(imageId: String, timestamp: Long = System.currentTimeMillis())
}