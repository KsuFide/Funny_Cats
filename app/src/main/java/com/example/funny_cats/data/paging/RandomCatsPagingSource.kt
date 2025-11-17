package com.example.funny_cats.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RandomCatsPagingSource(
    private val database: CatDatabase
) : PagingSource<Int, CatImage>() {

    private val imageDao = database.catImageDao()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CatImage> {
        return try {
            val page = params.key ?: 0
            val response = RetrofitInstance.api.getRandomCats(limit = params.loadSize)

            // Сохраняем изображения в базу данных с lastUpdated = 0
            val imagesWithTimestamp = response.map {
                it.copy(lastUpdated = 0L)
            }

            // ВСТАВЛЯЕМ ИЗОБРАЖЕНИЯ В БАЗУ ДАННЫХ
            imageDao.insertAll(imagesWithTimestamp)

            // ПОЛУЧАЕМ ДАННЫЕ ИЗ БАЗЫ, ЧТОБЫ ОНИ БЫЛИ СИНХРОНИЗИРОВАНЫ
            val imagesFromDb = withContext(Dispatchers.IO) {
                response.mapNotNull { apiImage ->
                    imageDao.getImageById(apiImage.id) ?: apiImage.copy(lastUpdated = 0L)
                }
            }

            LoadResult.Page(
                data = imagesFromDb, // ИСПОЛЬЗУЕМ ДАННЫЕ ИЗ БАЗЫ
                prevKey = if (page == 0) null else page - 1,
                nextKey = page + 1
            )
        } catch (e: Exception) {
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