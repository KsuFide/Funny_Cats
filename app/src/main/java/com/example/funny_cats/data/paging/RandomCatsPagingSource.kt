package com.example.funny_cats.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import android.util.Log

class RandomCatsPagingSource(
    private val database: CatDatabase
) : PagingSource<Int, CatImage>() {

    private val imageDao = database.catImageDao()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CatImage> {
        return try {
            val page = params.key ?: 0
            Log.d("PagingDebug", "Loading page $page with size ${params.loadSize}")

            val response = try {
                RetrofitInstance.api.getRandomCats(limit = params.loadSize)
            } catch (e: Exception) {
                Log.e("PagingDebug", "API call failed: ${e.message}")
                // Если API не работает, возвращаем данные из базы
                val cachedImages = withContext(Dispatchers.IO) {
                    imageDao.getAllImages().first() // Получаем данные из Flow
                }
                return LoadResult.Page(
                    data = cachedImages.take(params.loadSize),
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (cachedImages.size < params.loadSize) null else page + 1
                )
            }

            Log.d("PagingDebug", "API response size: ${response.size}")

            // Сохраняем изображения в базу данных с lastUpdated = 0
            val imagesWithTimestamp = response.map {
                it.copy(lastUpdated = 0L)
            }

            imageDao.insertAll(imagesWithTimestamp)

            // Получаем данные из базы
            val imagesFromDb = withContext(Dispatchers.IO) {
                response.mapNotNull { apiImage ->
                    imageDao.getImageById(apiImage.id) ?: apiImage.copy(lastUpdated = 0L)
                }
            }

            LoadResult.Page(
                data = imagesFromDb,
                prevKey = if (page == 0) null else page - 1,
                nextKey = page + 1
            )
        } catch (e: Exception) {
            Log.e("PagingDebug", "Load error: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CatImage>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}