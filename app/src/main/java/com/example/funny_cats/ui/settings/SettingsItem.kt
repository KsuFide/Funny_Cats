package com.example.funny_cats.ui.settings

sealed class SettingsItem {
    data class Header(val title: String) : SettingsItem()
    data class SwitchSetting(
        val id: String,
        val title: String,
        val description: String,
        val isChecked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsItem()

    data class ThemeSetting(
        val currentTheme: String,
        val onThemeSelected: (String) -> Unit
    ) : SettingsItem()

    data class InfoSetting(
        val title: String,
        val value: String,
        val showDivider: Boolean = true,
        val onClick: (() -> Unit)? = null
    ) : SettingsItem()
}