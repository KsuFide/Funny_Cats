package com.example.funny_cats.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.funny_cats.R
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.databinding.ItemBreedBinding
import com.example.funny_cats.databinding.ItemCatImageBinding
import com.example.funny_cats.util.RussianTranslator

sealed class CombinedHistoryItem {
    data class BreedItem(val breed: CatBreedEntity, val viewedAt: Long) : CombinedHistoryItem()
    data class ImageItem(val image: CatImage, val viewedAt: Long) : CombinedHistoryItem()
}

class CombinedHistoryAdapter(
    private val onBreedClick: (CatBreedEntity) -> Unit,
    private val onImageClick: (CatImage) -> Unit,
    private val onFavoriteClick: (String, Boolean) -> Unit
) : ListAdapter<CombinedHistoryItem, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val TYPE_BREED = 0
        private const val TYPE_IMAGE = 1

        object DiffCallback : DiffUtil.ItemCallback<CombinedHistoryItem>() {
            override fun areItemsTheSame(oldItem: CombinedHistoryItem, newItem: CombinedHistoryItem): Boolean {
                return when {
                    oldItem is CombinedHistoryItem.BreedItem && newItem is CombinedHistoryItem.BreedItem ->
                        oldItem.breed.id == newItem.breed.id
                    oldItem is CombinedHistoryItem.ImageItem && newItem is CombinedHistoryItem.ImageItem ->
                        oldItem.image.id == newItem.image.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: CombinedHistoryItem, newItem: CombinedHistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CombinedHistoryItem.BreedItem -> TYPE_BREED
            is CombinedHistoryItem.ImageItem -> TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_BREED -> BreedViewHolder(
                ItemBreedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            TYPE_IMAGE -> ImageViewHolder(
                ItemCatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CombinedHistoryItem.BreedItem -> (holder as BreedViewHolder).bind(item.breed, item.viewedAt)
            is CombinedHistoryItem.ImageItem -> (holder as ImageViewHolder).bind(item.image, item.viewedAt)
        }
    }

    // Очищаем ресурсы при переиспользовании ViewHolder
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is ImageViewHolder -> holder.clearImage()
            is BreedViewHolder -> holder.clearImage()
        }
    }

    // Очищаем все ресурсы адаптера
    fun clearResources() {
        // Очищаем все изображения Glide
        for (i in 0 until itemCount) {
            when (val item = getItem(i)) {
                is CombinedHistoryItem.ImageItem -> {
                    // Можно добавить логику очистки если нужно
                }
                is CombinedHistoryItem.BreedItem -> {
                    // Можно добавить логику очистки если нужно
                }
            }
        }
    }

    inner class BreedViewHolder(private val binding: ItemBreedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(breed: CatBreedEntity, viewedAt: Long) {
            with(binding) {
                // Загрузка изображения с обработкой ошибок
                try {
                    Glide.with(imageViewBreed.context)
                        .load(breed.imageId?.let { "https://cdn2.thecatapi.com/images/$it.jpg" })
                        .placeholder(R.drawable.ic_cat_placeholder)
                        .error(R.drawable.ic_cat_placeholder)
                        .centerCrop()
                        .into(imageViewBreed)
                } catch (e: Exception) {
                    e.printStackTrace()
                    imageViewBreed.setImageResource(R.drawable.ic_cat_placeholder)
                }

                // Русское название породы
                textViewBreedName.text = RussianTranslator.translateBreedName(breed.name)

                // Русское происхождение
                textViewBreedOrigin.text = RussianTranslator.translateOrigin(breed.origin)

                // Русский темперамент (сокращенный)
                textViewBreedTemperament.text = RussianTranslator.getShortTemperament(breed.temperament)

                // Время последнего просмотра
                textViewBreedTemperament.append("\n👁️ Просмотрено: ${getTimeAgo(viewedAt)}")

                // Обработчик клика
                root.setOnClickListener {
                    try {
                        onBreedClick(breed)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        fun clearImage() {
            try {
                Glide.with(binding.root).clear(binding.imageViewBreed)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class ImageViewHolder(private val binding: ItemCatImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: CatImage, viewedAt: Long) {
            try {
                // Загрузка изображения с обработкой ошибок
                Glide.with(binding.root)
                    .load(image.url)
                    .placeholder(R.drawable.ic_cat_placeholder)
                    .error(R.drawable.ic_cat_placeholder)
                    .centerCrop()
                    .into(binding.imageView)

                // Обновляем иконку избранного
                updateFavoriteIcon(image.isInFavorites)

                // Обработчик клика по изображению
                binding.imageView.setOnClickListener {
                    try {
                        onImageClick(image)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Обработчик клика по кнопке избранного
                binding.favoriteButton.setOnClickListener {
                    try {
                        val newFavoriteState = !image.isInFavorites
                        onFavoriteClick(image.id, newFavoriteState)
                        updateFavoriteIcon(newFavoriteState)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Добавляем обработчик долгого нажатия
                binding.imageView.setOnLongClickListener {
                    try {
                        showToast("Просмотрено: ${getTimeAgo(viewedAt)}")
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        true
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                binding.imageView.setImageResource(R.drawable.ic_cat_placeholder)
            }
        }

        fun clearImage() {
            try {
                Glide.with(binding.root).clear(binding.imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            try {
                val icon = if (isFavorite) {
                    R.drawable.ic_favorite_filled
                } else {
                    R.drawable.ic_favorite_border
                }
                binding.favoriteButton.setImageResource(icon)

                binding.favoriteButton.contentDescription =
                    if (isFavorite) "Удалить из избранного" else "Добавить в избранное"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun showToast(message: String) {
            try {
                android.widget.Toast.makeText(binding.root.context, message, android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getTimeAgo(timestamp: Long): String {
        return try {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            when {
                diff < 60000 -> "только что"
                diff < 3600000 -> "${diff / 60000} мин назад"
                diff < 86400000 -> "${diff / 3600000} ч назад"
                diff < 604800000 -> "${diff / 86400000} дн назад"
                else -> "${diff / 604800000} нед назад"
            }
        } catch (e: Exception) {
            "недавно"
        }
    }
}