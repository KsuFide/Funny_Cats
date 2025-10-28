package com.example.funny_cats.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.data.paging.RandomCatsPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    // Сохраняем текущий функционал для обратной совместимости
    private val _catImages = MutableStateFlow<List<CatImage>>(emptyList())
    val catImages = _catImages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Добавляем пагинацию
    val catsPagingFlow: Flow<PagingData<CatImage>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
            initialLoadSize = 20
        )
    ) {
        RandomCatsPagingSource()
    }.flow.cachedIn(viewModelScope)

    // Старый метод для обратной совместимости
    fun loadRandomCats(limit: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getRandomCats(limit)
                _catImages.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}