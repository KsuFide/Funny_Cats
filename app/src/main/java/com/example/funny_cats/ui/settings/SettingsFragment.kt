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
import com.example.funny_cats.data.local.model.ThemeMode
import com.example.funny_cats.util.ThemeHelper
import com.example.funny_cats.ui.settings.SettingsItem as SettingsItemModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

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
        val settingsList = mutableListOf<SettingsItemModel>()

        // Раздел уведомлений - ПЕРЕМЕЩАЕМ ВВЕРХ
        settingsList.add(SettingsItemModel.Header("🔔 Уведомления"))
        settingsList.add(
            SettingsItemModel.SwitchSetting(
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

        // ДОБАВЛЯЕМ КЛИКАБЕЛЬНЫЙ ПУНКТ ДЛЯ ПЕРЕХОДА К НАСТРОЙКАМ УВЕДОМЛЕНИЙ
        settingsList.add(
            SettingsItemModel.InfoSetting(
                title = "Настройки уведомлений",
                value = "Настроить типы уведомлений",
                showDivider = true,
                onClick = {
                    // Переход к фрагменту уведомлений
                    findNavController().navigate(R.id.action_settingsFragment_to_notificationsFragment)
                }
            )
        )

        // Раздел контента
        settingsList.add(SettingsItemModel.Header("📱 Контент"))
        settingsList.add(
            SettingsItemModel.SwitchSetting(
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
            SettingsItemModel.SwitchSetting(
                id = "breed_images",
                title = "Изображения пород",
                description = "Показывать изображения для каждой породы",
                isChecked = settings.showBreedImages,
                onCheckedChange = { enabled ->
                    viewModel.updateBreedImagesSetting(enabled)
                }
            )
        )

        // Раздел внешнего вида
        settingsList.add(SettingsItemModel.Header("🎨 Внешний вид"))
        settingsList.add(
            SettingsItemModel.ThemeSetting(
                currentTheme = settings.themeMode.name,
                onThemeSelected = { theme ->
                    viewModel.updateThemeMode(ThemeMode.valueOf(theme))
                    ThemeHelper.applyTheme(theme)
                    ThemeHelper.saveThemePreference(requireContext(), theme)
                }
            )
        )

        // Экспериментальные функции
        settingsList.add(SettingsItemModel.Header("🧪 Экспериментальные функции"))
        settingsList.add(
            SettingsItemModel.SwitchSetting(
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
        settingsList.add(SettingsItemModel.Header("ℹ️ О приложении"))
        settingsList.add(
            SettingsItemModel.InfoSetting(
                title = "Версия",
                value = "1.0.0",
                showDivider = true
            )
        )
        settingsList.add(
            SettingsItemModel.InfoSetting(
                title = "Разработчик",
                value = "CatFinder Team",
                showDivider = true
            )
        )
        settingsList.add(
            SettingsItemModel.InfoSetting(
                title = "Источник данных",
                value = "The Cat API",
                showDivider = false
            )
        )

        adapter.submitList(settingsList)
    }

    private fun showRestartDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.restart_dialog_title))
            .setMessage(getString(R.string.restart_dialog_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}