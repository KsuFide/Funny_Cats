package com.example.funny_cats.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatBreed
import com.example.funny_cats.data.local.model.CatBreedEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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

            // Получаем текущие породы из базы чтобы сохранить историю просмотров
            val existingBreeds = dao.getAllBreeds().first()
            val existingBreedsMap = existingBreeds.associateBy { it.id }

            val entities = breedsFromApi.map { apiBreed ->
                val existingBreed = existingBreedsMap[apiBreed.id]

                // Сохраняем данные из существующей породы если есть
                if (existingBreed != null) {
                    existingBreed.copy(
                        name = apiBreed.name,
                        origin = apiBreed.origin,
                        temperament = apiBreed.temperament,
                        description = apiBreed.description,
                        wikipediaUrl = apiBreed.wikipediaUrl,
                        imageId = apiBreed.imageId,
                        lifeSpan = apiBreed.lifeSpan,
                        intelligence = apiBreed.intelligence,
                        dogFriendly = apiBreed.dogFriendly,
                        adaptability = apiBreed.adaptability
                        // lastViewed сохраняется из existingBreed
                    )
                } else {
                    // Новая порода
                    apiBreed.toEntity()
                }
            }

            dao.insertAll(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearCache() {
        dao.clearAll()
    }

    // Обновляем время просмотра породы
    suspend fun updateBreedViewTime(breedId: String) {
        dao.updateViewTime(breedId, System.currentTimeMillis())
    }

    // Получаем породу по ID
    suspend fun getBreedById(breedId: String): CatBreedEntity? {
        return dao.getBreedById(breedId)
    }

    // Сохраняем породу в базу (если ее нет) или обновляем существующую
    suspend fun saveOrUpdateBreed(breed: CatBreed) {
        val existingBreed = dao.getBreedById(breed.id)

        if (existingBreed != null) {
            // Обновляем существующую породу, но сохраняем lastViewed
            val updatedBreed = existingBreed.copy(
                name = breed.name,
                origin = breed.origin,
                temperament = breed.temperament,
                description = breed.description,
                wikipediaUrl = breed.wikipediaUrl,
                imageId = breed.imageId,
                lifeSpan = breed.lifeSpan,
                intelligence = breed.intelligence,
                dogFriendly = breed.dogFriendly,
                adaptability = breed.adaptability
                // lastViewed сохраняется из existingBreed
            )
            dao.updateBreed(updatedBreed)
        } else {
            // Новая порода
            dao.insertAll(listOf(breed.toEntity()))
        }
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
        adaptability = this.adaptability,
        lastViewed = 0L
    )
}

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