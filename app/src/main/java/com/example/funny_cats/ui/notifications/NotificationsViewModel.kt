package com.example.funny_cats.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.funny_cats.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val notificationSettings = notificationRepository.getAllSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            notificationRepository.initializeDefaultSettings()
        }
    }

    fun updateSetting(settingId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = notificationSettings.value
            val settingToUpdate = currentSettings.find { it.id == settingId }
            settingToUpdate?.let { setting ->
                notificationRepository.updateSetting(setting.copy(isEnabled = isEnabled))
            }
        }
    }

    // Отправка тестового уведомления для конкретного типа
    fun sendTestNotification(settingId: String) {
        notificationRepository.sendTestNotification(settingId)
    }

    // Проверка и отправка умных уведомлений
    fun checkSmartNotifications() {
        viewModelScope.launch {
            notificationRepository.checkAndSendSmartNotifications()
        }
    }
}