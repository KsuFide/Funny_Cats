package com.example.funny_cats.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.funny_cats.data.local.CatDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val database: CatDatabase
) : ViewModel() {

    private val imageDao = database.catImageDao()

    // История просмотров - изображения отсортированные по времени просмотра
    val historyImagesPaging: Flow<PagingData<com.example.funny_cats.data.local.model.CatImage>> =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            )
        ) {
            imageDao.getHistoryPagingSource()
        }.flow.cachedIn(viewModelScope)

    fun toggleImageFavorite(imageId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            imageDao.updateFavoriteStatus(imageId, isFavorite)
        }
    }
}