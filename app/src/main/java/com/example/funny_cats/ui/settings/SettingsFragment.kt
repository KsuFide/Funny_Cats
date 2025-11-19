package com.example.funny_cats.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.funny_cats.R
import com.example.funny_cats.databinding.FragmentSettingsBinding
import com.example.funny_cats.ui.BaseFragment
import com.example.funny_cats.util.ThemeHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var adapter: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        observeSettings()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = SettingsAdapter()
        binding.recyclerViewSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSettings.adapter = adapter
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settings.collectLatest { settings ->
                updateSettingsList(settings)
            }
        }
    }

    private fun updateSettingsList(settings: com.example.funny_cats.data.local.model.AppSettings) {
        val settingsList = mutableListOf<SettingsItem>()

        // Раздел уведомлений
        settingsList.add(SettingsItem.Header("🔔 Уведомления"))
        settingsList.add(
            SettingsItem.SwitchSetting(
                id = "notifications",
                title = "Уведомления",
                description = "Включить все уведомления",
                isChecked = settings.notificationsEnabled,
                onCheckedChange = { enabled ->
                    viewModel.updateNotificationsEnabled(enabled)
                    showToast(if (enabled) "Уведомления включены" else "Уведомления выключены")
                }
            )
        )

        // Кликабельный пункт для настроек уведомлений
        settingsList.add(
            SettingsItem.InfoSetting(
                title = "Настройки уведомлений",
                value = "Настроить типы уведомлений",
                showDivider = true,
                onClick = {
                    findNavController().navigate(R.id.action_settingsFragment_to_notificationsFragment)
                }
            )
        )

        // Раздел внешнего вида
        settingsList.add(SettingsItem.Header("🎨 Внешний вид"))
        settingsList.add(
            SettingsItem.ThemeSetting(
                currentTheme = getCurrentThemeName(),
                onThemeSelected = { theme ->
                    applyTheme(theme)
                }
            )
        )

        // Раздел контента
        settingsList.add(SettingsItem.Header("📱 Контент"))
        settingsList.add(
            SettingsItem.SwitchSetting(
                id = "random_cats",
                title = "Случайные котики",
                description = "Показывать случайных котиков в ленте",
                isChecked = settings.showRandomCats,
                onCheckedChange = { enabled ->
                    viewModel.updateRandomCatsSetting(enabled)
                }
            )
        )
        settingsList.add(
            SettingsItem.SwitchSetting(
                id = "breed_images",
                title = "Изображения пород",
                description = "Показывать изображения для каждой породы",
                isChecked = settings.showBreedImages,
                onCheckedChange = { enabled ->
                    viewModel.updateBreedImagesSetting(enabled)
                }
            )
        )

        // Экспериментальные функции
        settingsList.add(SettingsItem.Header("🧪 Экспериментальные функции"))
        settingsList.add(
            SettingsItem.SwitchSetting(
                id = "compose",
                title = "Использовать Compose",
                description = "Включить экспериментальный UI на Compose",
                isChecked = settings.useCompose,
                onCheckedChange = { enabled ->
                    viewModel.updateComposeSetting(enabled)
                    showRestartDialog()
                }
            )
        )

        // О приложении
        settingsList.add(SettingsItem.Header("ℹ️ О приложении"))
        settingsList.add(
            SettingsItem.InfoSetting(
                title = "Версия",
                value = "1.0.0",
                showDivider = true
            )
        )
        settingsList.add(
            SettingsItem.InfoSetting(
                title = "Разработчик",
                value = "CatFinder Team",
                showDivider = true
            )
        )
        settingsList.add(
            SettingsItem.InfoSetting(
                title = "Источник данных",
                value = "The Cat API",
                showDivider = false
            )
        )

        adapter.submitList(settingsList)
    }

    private fun getCurrentThemeName(): String {
        return when (ThemeHelper.getSavedTheme(requireContext())) {
            "LIGHT" -> "Светлая"
            "DARK" -> "Тёмная"
            else -> "Системная"
        }
    }

    private fun applyTheme(theme: String) {
        val themeValue = when (theme) {
            "Светлая" -> "LIGHT"
            "Тёмная" -> "DARK"
            else -> "SYSTEM"
        }

        ThemeHelper.applyTheme(themeValue)
        ThemeHelper.saveThemePreference(requireContext(), themeValue)

        // Перезапускаем активность для применения темы
        requireActivity().recreate()
    }

    private fun showRestartDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Перезапуск приложения")
            .setMessage("Для применения некоторых настроек требуется перезапуск приложения")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.recyclerViewSettings?.adapter = null
        _binding = null
    }
}