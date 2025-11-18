package com.example.funny_cats.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.funny_cats.R
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.databinding.ItemBreedBinding
import com.example.funny_cats.util.RussianTranslator

class BreedHistoryAdapter(
    private val onItemClick: (CatBreedEntity) -> Unit,
    private val onFavoriteClick: (CatBreedEntity, Boolean) -> Unit
) : PagingDataAdapter<CatBreedEntity, BreedHistoryAdapter.BreedHistoryViewHolder>(DiffCallback) {

    inner class BreedHistoryViewHolder(private val binding: ItemBreedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(breed: CatBreedEntity?) {
            breed?.let { catBreed ->
                with(binding) {
                    // Загрузка изображения
                    Glide.with(imageViewBreed.context)
                        .load(catBreed.imageId?.let { "https://cdn2.thecatapi.com/images/$it.jpg" })
                        .placeholder(R.drawable.ic_cat_placeholder)
                        .error(R.drawable.ic_cat_placeholder)
                        .into(imageViewBreed)

                    // Русское название породы
                    textViewBreedName.text = RussianTranslator.translateBreedName(catBreed.name)

                    // Русское происхождение
                    textViewBreedOrigin.text = RussianTranslator.translateOrigin(catBreed.origin)

                    // Русский темперамент (сокращенный)
                    textViewBreedTemperament.text = RussianTranslator.getShortTemperament(catBreed.temperament)

                    // Время последнего просмотра
                    textViewBreedTemperament.append("\n👁️ Просмотрено: ${getTimeAgo(catBreed.lastViewed)}")

                    // Обработчик клика с безопасностью
                    root.setOnClickListener {
                        try {
                            onItemClick(catBreed)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // Кнопка избранного
                    val favoriteIcon = if (catBreed.isInFavorites) {
                        R.drawable.ic_favorite_filled
                    } else {
                        R.drawable.ic_favorite_border
                    }
                    // Здесь можно добавить ImageButton для избранного если нужно
                }
            }
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "только что"
                diff < 3600000 -> "${diff / 60000} мин назад"
                diff < 86400000 -> "${diff / 3600000} ч назад"
                diff < 604800000 -> "${diff / 86400000} дн назад"
                else -> "${diff / 604800000} нед назад"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreedHistoryViewHolder {
        val binding = ItemBreedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BreedHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BreedHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CatBreedEntity>() {
        override fun areItemsTheSame(oldItem: CatBreedEntity, newItem: CatBreedEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CatBreedEntity, newItem: CatBreedEntity): Boolean {
            return oldItem == newItem
        }
    }
}