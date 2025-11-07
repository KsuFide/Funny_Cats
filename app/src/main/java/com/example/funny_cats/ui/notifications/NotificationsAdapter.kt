package com.example.funny_cats.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.funny_cats.data.local.model.NotificationSetting
import com.example.funny_cats.databinding.ItemNotificationSettingBinding

class NotificationsAdapter(
    private val onSettingChanged: (NotificationSetting, Boolean) -> Unit,
    private val onTestClick: (NotificationSetting) -> Unit
) : ListAdapter<NotificationSetting, NotificationsAdapter.NotificationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationSettingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationSettingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(setting: NotificationSetting) {
            binding.apply {
                textTitle.text = setting.title
                textDescription.text = setting.description
                switchNotification.isChecked = setting.isEnabled

                // Время для уведомлений по расписанию
                val timeText = when (setting.id) {
                    NotificationSetting.DAILY_REMINDER_ID -> "Ежедневно в 20:00"
                    NotificationSetting.WEEKLY_SUMMARY_ID -> "По воскресеньям в 18:00"
                    NotificationSetting.INACTIVITY_REMINDER_ID -> "При неактивности 3 дня"
                    else -> "По событиям"
                }
                textTime.text = timeText

                switchNotification.setOnCheckedChangeListener { _, isChecked ->
                    onSettingChanged(setting, isChecked)
                }

                // Кнопка тестирования для каждого типа
                buttonTest.setOnClickListener {
                    onTestClick(setting)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NotificationSetting>() {
        override fun areItemsTheSame(oldItem: NotificationSetting, newItem: NotificationSetting): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationSetting, newItem: NotificationSetting): Boolean {
            return oldItem == newItem
        }
    }
}