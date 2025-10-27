package com.example.funny_cats.ui.breeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.model.CatBreed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedsViewModel @Inject constructor() : ViewModel() {

    private val _breeds = MutableStateFlow<List<CatBreed>>(emptyList())
    val breeds = _breeds.asStateFlow()

    private val _filteredBreeds = MutableStateFlow<List<CatBreed>>(emptyList())
    val filteredBreeds = _filteredBreeds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadAllBreeds()
    }

    fun loadAllBreeds() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getAllBreeds()
                _breeds.value = response
                _filteredBreeds.value = response
                println("DEBUG: Загружено ${response.size} пород") // Для отладки
            } catch (e: Exception) {
                e.printStackTrace()
                println("DEBUG: Ошибка загрузки пород: ${e.message}") // Для отладки
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchBreeds(query: String) {
        _searchQuery.value = query
        println("DEBUG: Поиск запроса: '$query'") // Для отладки

        if (query.isEmpty()) {
            _filteredBreeds.value = _breeds.value
            println("DEBUG: Пустой запрос, показано всех: ${_breeds.value.size}") // Для отладки
        } else {
            val filtered = _breeds.value.filter { breed ->
                // Проверяем все текстовые поля на совпадение
                breed.name.contains(query, ignoreCase = true) ||
                        breed.origin?.contains(query, ignoreCase = true) == true ||
                        breed.temperament?.contains(query, ignoreCase = true) == true ||
                        breed.description?.contains(query, ignoreCase = true) == true ||
                        breed.lifeSpan?.contains(query, ignoreCase = true) == true
            }
            _filteredBreeds.value = filtered
            println("DEBUG: Найдено пород: ${filtered.size}") // Для отладки
            filtered.forEach { breed ->
                println("DEBUG: Найдена порода: ${breed.name}") // Для отладки
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _filteredBreeds.value = _breeds.value
    }
}