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

        // Переносим DiffCallback внутрь companion object
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

    inner class BreedViewHolder(private val binding: ItemBreedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(breed: CatBreedEntity, viewedAt: Long) {
            with(binding) {
                // Загрузка изображения
                Glide.with(imageViewBreed.context)
                    .load(breed.imageId?.let { "https://cdn2.thecatapi.com/images/$it.jpg" })
                    .placeholder(R.drawable.ic_cat_placeholder)
                    .error(R.drawable.ic_cat_placeholder)
                    .into(imageViewBreed)

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
                    onBreedClick(breed)
                }
            }
        }
    }

    inner class ImageViewHolder(private val binding: ItemCatImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: CatImage, viewedAt: Long) {
            // Загрузка изображения
            Glide.with(binding.root)
                .load(image.url)
                .centerCrop()
                .into(binding.imageView)

            // Обновляем иконку избранного
            updateFavoriteIcon(image.isInFavorites)

            // Обработчик клика по изображению
            binding.imageView.setOnClickListener {
                onImageClick(image)
            }

            // Обработчик клика по кнопке избранного
            binding.favoriteButton.setOnClickListener {
                val newFavoriteState = !image.isInFavorites
                onFavoriteClick(image.id, newFavoriteState)
                updateFavoriteIcon(newFavoriteState)
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            val icon = if (isFavorite) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_border
            }
            binding.favoriteButton.setImageResource(icon)
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