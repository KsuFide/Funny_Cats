package com.example.funny_cats.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.paging.RandomCatsPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val database: CatDatabase
) : ViewModel() {

    private val imageDao = database.catImageDao()

    // Пагинация с сохранением в базу
    val catsPagingFlow: Flow<PagingData<com.example.funny_cats.data.local.model.CatImage>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
            initialLoadSize = 20
        )
    ) {
        RandomCatsPagingSource(database)
    }.flow.cachedIn(viewModelScope)

    // Метод для сохранения избранного в базу
    fun toggleImageFavorite(imageId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            imageDao.updateFavoriteStatus(imageId, isFavorite)

            // Для немедленного обновления UI в избранном
            // Можно добавить механизм обновления, но пока просто логируем
            println("DEBUG: Updated favorite status for image $imageId: $isFavorite")
        }
    }

    // Метод для обновления времени просмотра
    fun updateViewTime(imageId: String) {
        viewModelScope.launch {
            database.catImageDao().updateViewTime(imageId)
        }
    }

    suspend fun getImageById(imageId: String): com.example.funny_cats.data.local.model.CatImage? {
        return database.catImageDao().getImageById(imageId)
    }

    // Старый метод для обратной совместимости
    fun loadRandomCats(limit: Int = 10) {
        // Оставляем пустым
    }
}