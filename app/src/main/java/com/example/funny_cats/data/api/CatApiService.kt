package com.example.funny_cats.data.api

import com.example.funny_cats.data.model.CatBreed
import com.example.funny_cats.data.model.CatImage
import retrofit2.http.GET
import retrofit2.http.Query

interface CatApiService {

    @GET("v1/images/search")
    suspend fun getRandomCats(
        @Query("limit") limit: Int = 10,
        @Query("size") size: String = "med"
    ): List<CatImage>

    @GET("v1/breeds")
    suspend fun getAllBreeds(): List<CatBreed>

    @GET("v1/images/search")
    suspend fun getBreedImages(
        @Query("breed_ids") breedId: String,
        @Query("limit") limit: Int = 10,
        @Query("size") size: String = "med"
    ): List<CatImage>

    @GET("v1/breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String
    ): List<CatBreed>
}