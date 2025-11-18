package com.example.funny_cats.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatBreed
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.data.repository.CatBreedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedHistoryViewModel @Inject constructor(
    private val database: CatDatabase,
    private val breedRepository: CatBreedRepository
) : ViewModel() {

    private val breedDao = database.catBreedDao()

    // Пагинация для истории просмотренных пород
    val viewedBreedsPaging: Flow<PagingData<CatBreedEntity>> =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            )
        ) {
            breedDao.getViewedBreedsPagingSource()
        }.flow.cachedIn(viewModelScope)

    // Обновление статуса избранного для породы
    fun toggleBreedFavorite(breedId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            breedDao.updateFavoriteStatus(breedId, isFavorite)
        }
    }

    // Очистка истории просмотров пород
    fun clearHistory() {
        viewModelScope.launch {
            breedDao.clearViewHistory()
        }
    }

    // Обновление времени просмотра породы
    fun updateBreedViewTime(breedId: String) {
        viewModelScope.launch {
            breedRepository.updateBreedViewTime(breedId)
        }
    }

    // Сохраняем породу в базу данных
    fun saveBreedToDatabase(breed: CatBreed) {
        viewModelScope.launch {
            breedRepository.saveOrUpdateBreed(breed)
        }
    }
}