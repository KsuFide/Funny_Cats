package com.example.funny_cats.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.funny_cats.data.local.model.AppSettings
import com.example.funny_cats.data.local.model.ThemeMode
import com.example.funny_cats.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }

    fun updateRandomCatsSetting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRandomCatsSetting(enabled)
        }
    }

    fun updateBreedImagesSetting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBreedImagesSetting(enabled)
        }
    }

    fun updateComposeSetting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateComposeSetting(enabled)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotificationsEnabled(enabled)
        }
    }



    fun updateAllSettings(settings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings)
        }
    }
}