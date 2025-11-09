package com.example.funny_cats.ui.breeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.funny_cats.data.repository.CatBreedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedsViewModel @Inject constructor(
    private val repository: CatBreedRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val breedsPaging = _searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) {
            repository.getBreedsPaging()
        } else {
            repository.searchBreedsPaging(query)
        }
    }.cachedIn(viewModelScope)

    fun loadBreeds() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshBreeds()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchBreeds(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}