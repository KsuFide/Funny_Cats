package com.example.funny_cats.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.funny_cats.R
import com.example.funny_cats.databinding.ItemSettingHeaderBinding
import com.example.funny_cats.databinding.ItemSettingInfoBinding
import com.example.funny_cats.databinding.ItemSettingSwitchBinding
import com.example.funny_cats.databinding.ItemSettingThemeBinding

class SettingsAdapter : ListAdapter<SettingsItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SettingsItem.Header -> TYPE_HEADER
            is SettingsItem.SwitchSetting -> TYPE_SWITCH
            is SettingsItem.ThemeSetting -> TYPE_THEME
            is SettingsItem.InfoSetting -> TYPE_INFO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemSettingHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_SWITCH -> SwitchViewHolder(
                ItemSettingSwitchBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_THEME -> ThemeViewHolder(
                ItemSettingThemeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_INFO -> InfoViewHolder(
                ItemSettingInfoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is SettingsItem.Header -> (holder as HeaderViewHolder).bind(item)
            is SettingsItem.SwitchSetting -> (holder as SwitchViewHolder).bind(item)
            is SettingsItem.ThemeSetting -> (holder as ThemeViewHolder).bind(item)
            is SettingsItem.InfoSetting -> (holder as InfoViewHolder).bind(item)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemSettingHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem.Header) {
            binding.root.text = item.title
        }
    }

    inner class SwitchViewHolder(private val binding: ItemSettingSwitchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem.SwitchSetting) {
            binding.textTitle.text = item.title
            binding.textDescription.text = item.description
            binding.switchSetting.isChecked = item.isChecked

            binding.switchSetting.setOnCheckedChangeListener { _, isChecked ->
                item.onCheckedChange(isChecked)
            }

            binding.root.setOnClickListener {
                binding.switchSetting.isChecked = !binding.switchSetting.isChecked
            }
        }
    }

    inner class ThemeViewHolder(private val binding: ItemSettingThemeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem.ThemeSetting) {
            binding.textTitle.text = "Тема приложения"
            binding.textCurrentTheme.text = item.currentTheme

            // ДЕЛАЕМ КЛИКАБЕЛЬНЫМ - добавляем обработчик клика
            binding.root.setOnClickListener {
                showThemeDialog(item.onThemeSelected)
            }

            // Добавляем визуальную индикацию кликабельности
            binding.root.isClickable = true
            binding.root.isFocusable = true

            // Добавляем стрелочку для индикации
            binding.textCurrentTheme.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_forward, 0)
            binding.textCurrentTheme.compoundDrawablePadding = 16
        }

        private fun showThemeDialog(onThemeSelected: (String) -> Unit) {
            val themes = arrayOf("Светлая", "Тёмная", "Системная")
            val currentTheme = binding.textCurrentTheme.text.toString()

            androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
                .setTitle("Выберите тему")
                .setSingleChoiceItems(themes, themes.indexOf(currentTheme)) { dialog, which ->
                    onThemeSelected(themes[which])
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    inner class InfoViewHolder(private val binding: ItemSettingInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem.InfoSetting) {
            binding.textTitle.text = item.title
            binding.textValue.text = item.value

            binding.divider.visibility = if (item.showDivider) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                item.onClick?.invoke()
            }
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_SWITCH = 1
        private const val TYPE_THEME = 2
        private const val TYPE_INFO = 3

        object DiffCallback : DiffUtil.ItemCallback<SettingsItem>() {
            override fun areItemsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
                return when {
                    oldItem is SettingsItem.Header && newItem is SettingsItem.Header ->
                        oldItem.title == newItem.title
                    oldItem is SettingsItem.SwitchSetting && newItem is SettingsItem.SwitchSetting ->
                        oldItem.id == newItem.id
                    oldItem is SettingsItem.ThemeSetting && newItem is SettingsItem.ThemeSetting ->
                        true
                    oldItem is SettingsItem.InfoSetting && newItem is SettingsItem.InfoSetting ->
                        oldItem.title == newItem.title
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}