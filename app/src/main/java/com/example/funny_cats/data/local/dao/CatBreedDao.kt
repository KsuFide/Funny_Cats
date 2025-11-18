package com.example.funny_cats.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.funny_cats.data.local.model.CatBreedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatBreedDao {

    // Для пагинации
    @Query("SELECT * FROM cat_breeds ORDER BY name")
    fun getPagingSource(): androidx.paging.PagingSource<Int, CatBreedEntity>

    // Для поиска с пагинацией
    @Query("SELECT * FROM cat_breeds WHERE name LIKE '%' || :query || '%' OR origin LIKE '%' || :query || '%' OR temperament LIKE '%' || :query || '%' ORDER BY name")
    fun searchPagingSource(query: String): androidx.paging.PagingSource<Int, CatBreedEntity>

    // Для истории просмотров пород
    @Query("SELECT * FROM cat_breeds WHERE lastViewed > 0 ORDER BY lastViewed DESC")
    fun getViewedBreedsPagingSource(): androidx.paging.PagingSource<Int, CatBreedEntity>

    // методы для обратной совместимости
    @Query("SELECT * FROM cat_breeds ORDER BY name")
    fun getAllBreeds(): Flow<List<CatBreedEntity>>

    @Query("SELECT * FROM cat_breeds WHERE isInFavorites = 1 ORDER BY name")
    fun getFavoriteBreeds(): Flow<List<CatBreedEntity>>

    @Query("SELECT * FROM cat_breeds WHERE name LIKE '%' || :query || '%' OR origin LIKE '%' || :query || '%' OR temperament LIKE '%' || :query || '%' ORDER BY name")
    fun searchBreeds(query: String): Flow<List<CatBreedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breeds: List<CatBreedEntity>)

    @Update
    suspend fun updateBreed(breed: CatBreedEntity)

    @Query("UPDATE cat_breeds SET isInFavorites = :isFavorite WHERE id = :breedId")
    suspend fun updateFavoriteStatus(breedId: String, isFavorite: Boolean)

    @Query("UPDATE cat_breeds SET isInWatchLater = :inWatchLater WHERE id = :breedId")
    suspend fun updateWatchLaterStatus(breedId: String, inWatchLater: Boolean)

    @Query("DELETE FROM cat_breeds")
    suspend fun clearAll()

    @Query("SELECT * FROM cat_breeds WHERE id = :breedId")
    suspend fun getBreedById(breedId: String): CatBreedEntity?

    // Обновляем время просмотра породы
    @Query("UPDATE cat_breeds SET lastViewed = :timestamp WHERE id = :breedId")
    suspend fun updateViewTime(breedId: String, timestamp: Long = System.currentTimeMillis())

    // Очистка истории просмотров пород
    @Query("UPDATE cat_breeds SET lastViewed = 0")
    suspend fun clearViewHistory()
}