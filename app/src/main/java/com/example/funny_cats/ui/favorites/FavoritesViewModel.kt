package com.example.funny_cats.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.CatImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val database: CatDatabase
) : ViewModel() {

    private val imageDao = database.catImageDao()

    // ЯВНО УКАЗЫВАЕМ ТИПЫ ДЛЯ PAGING DATA
    val favoriteImagesPaging: Flow<PagingData<CatImage>> =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            )
        ) {
            // ЯВНО УКАЗЫВАЕМ ТИП PAGING SOURCE
            imageDao.getFavoriteImagesPagingSource()
        }.flow.cachedIn(viewModelScope)

    fun toggleImageFavorite(imageId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            imageDao.updateFavoriteStatus(imageId, isFavorite)
        }
    }
}