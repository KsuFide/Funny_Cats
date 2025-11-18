package com.example.funny_cats.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val database: CatDatabase,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val breedDao = database.catBreedDao()
    private val imageDao = database.catImageDao()

    // Текущий тип истории (0 - все, 1 - породы, 2 - изображения)
    private val _currentHistoryType = MutableStateFlow(0)
    val currentHistoryType = _currentHistoryType.asStateFlow()

    // Объединенная история
    val combinedHistory = historyRepository.getCombinedHistory()

    // История пород
    val breedHistory = historyRepository.getBreedHistory()

    // История изображений
    val imageHistory = historyRepository.getImageHistory()

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

    // Пагинация для истории просмотренных изображений
    val viewedImagesPaging: Flow<PagingData<CatImage>> =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            )
        ) {
            imageDao.getHistoryPagingSource()
        }.flow.cachedIn(viewModelScope)

    // Обновление статуса избранного для породы
    fun toggleBreedFavorite(breedId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            breedDao.updateFavoriteStatus(breedId, isFavorite)
        }
    }

    // Обновление статуса избранного для изображения
    fun toggleImageFavorite(imageId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            imageDao.updateFavoriteStatus(imageId, isFavorite)
        }
    }

    // Очистка всей истории просмотров
    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
        }
    }

    // Очистка истории пород
    fun clearBreedHistory() {
        viewModelScope.launch {
            historyRepository.clearBreedHistory()
        }
    }

    // Очистка истории изображений
    fun clearImageHistory() {
        viewModelScope.launch {
            historyRepository.clearImageHistory()
        }
    }

    // Установка типа истории
    fun setHistoryType(type: Int) {
        _currentHistoryType.value = type
    }
}