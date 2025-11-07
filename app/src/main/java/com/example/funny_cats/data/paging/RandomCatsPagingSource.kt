package com.example.funny_cats.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatImage

class RandomCatsPagingSource(
    private val database: CatDatabase
) : PagingSource<Int, CatImage>() {

    private val imageDao = database.catImageDao()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CatImage> {
        return try {
            val page = params.key ?: 0
            val response = RetrofitInstance.api.getRandomCats(limit = params.loadSize)

            // Сохраняем изображения в базу данных
            val imagesWithTimestamp = response.map {
                it.copy(lastUpdated = System.currentTimeMillis())
            }
            imageDao.insertAll(imagesWithTimestamp)

            LoadResult.Page(
                data = response,
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