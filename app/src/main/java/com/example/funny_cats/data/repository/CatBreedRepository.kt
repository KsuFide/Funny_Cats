package com.example.funny_cats.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatBreed
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CatBreedRepository(private val database: CatDatabase) {

    private val dao = database.catBreedDao()

    fun getBreedsPaging(): Flow<PagingData<CatBreedEntity>> {
        Logger.d("Getting breeds paging flow")
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
        Logger.d("Searching breeds with query: $query")
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
            Logger.d("Refreshing breeds from API")
            val breedsFromApi = RetrofitInstance.api.getAllBreeds()

            val existingBreeds = dao.getAllBreeds().first()
            val existingBreedsMap = existingBreeds.associateBy { it.id }

            val entities = breedsFromApi.map { apiBreed ->
                val existingBreed = existingBreedsMap[apiBreed.id]

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
                    )
                } else {
                    apiBreed.toEntity()
                }
            }

            dao.insertAll(entities)
            Logger.d("Successfully refreshed ${entities.size} breeds")
        } catch (e: Exception) {
            Logger.e("Failed to refresh breeds: ${e.message}", e)
        }
    }

    suspend fun clearCache() {
        try {
            dao.clearAll()
            Logger.d("Successfully cleared breed cache")
        } catch (e: Exception) {
            Logger.e("Failed to clear breed cache: ${e.message}", e)
        }
    }

    suspend fun updateBreedViewTime(breedId: String) {
        try {
            dao.updateViewTime(breedId, System.currentTimeMillis())
            Logger.d("Updated view time for breed: $breedId")
        } catch (e: Exception) {
            Logger.e("Failed to update view time for breed $breedId: ${e.message}", e)
        }
    }

    suspend fun getBreedById(breedId: String): CatBreedEntity? {
        return try {
            dao.getBreedById(breedId)
        } catch (e: Exception) {
            Logger.e("Failed to get breed by id $breedId: ${e.message}", e)
            null
        }
    }

    suspend fun saveOrUpdateBreed(breed: CatBreed) {
        val existingBreed = dao.getBreedById(breed.id)

        if (existingBreed != null) {
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
            )
            dao.updateBreed(updatedBreed)
            Logger.d("Updated existing breed: ${breed.name}")
        } else {
            dao.insertAll(listOf(breed.toEntity()))
            Logger.d("Saved new breed: ${breed.name}")
        }
    }
}

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