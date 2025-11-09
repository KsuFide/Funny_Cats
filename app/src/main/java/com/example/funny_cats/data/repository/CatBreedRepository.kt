package com.example.funny_cats.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatBreed
import com.example.funny_cats.data.local.model.CatBreedEntity
import kotlinx.coroutines.flow.Flow

class CatBreedRepository(private val database: CatDatabase) {

    private val dao = database.catBreedDao()

    fun getBreedsPaging(): Flow<PagingData<CatBreedEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 100
            ),
            pagingSourceFactory = {
                dao.getPagingSource()
            }
        ).flow
    }

    fun searchBreedsPaging(query: String): Flow<PagingData<CatBreedEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 100
            ),
            pagingSourceFactory = {
                dao.searchPagingSource(query)
            }
        ).flow
    }

    suspend fun refreshBreeds() {
        try {
            val breedsFromApi = RetrofitInstance.api.getAllBreeds()
            val entities = breedsFromApi.map { it.toEntity() }
            dao.insertAll(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearCache() {
        dao.clearAll()
    }
}

// Конвертация без избранного
private fun CatBreed.toEntity(): CatBreedEntity {
    return CatBreedEntity(
        id = this.id,
        name = this.name,
        origin = this.origin,
        temperament = this.temperament,
        description = this.description,
        wikipediaUrl = this.wikipediaUrl,
        imageId = this.imageId,
        lifeSpan = this.lifeSpan,
        intelligence = this.intelligence,
        dogFriendly = this.dogFriendly,
        adaptability = this.adaptability
    )}

    // Расширение для конвертации Entity в CatBreed (для деталей породы)
    fun CatBreedEntity.toCatBreed(): CatBreed {
        return CatBreed(
            id = this.id,
            name = this.name,
            origin = this.origin,
            temperament = this.temperament,
            description = this.description,
            wikipediaUrl = this.wikipediaUrl,
            imageId = this.imageId,
            lifeSpan = this.lifeSpan,
            intelligence = this.intelligence,
            dogFriendly = this.dogFriendly,
            adaptability = this.adaptability
        )
}