package com.example.funny_cats.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.model.CatImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _catImages = MutableStateFlow<List<CatImage>>(emptyList())
    val catImages = _catImages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadRandomCats(limit: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getRandomCats(limit)
                _catImages.value = response
            } catch (e: Exception) {
                // Обработка ошибок
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}