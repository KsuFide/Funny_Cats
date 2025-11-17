package com.example.funny_cats.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.funny_cats.R
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.databinding.ItemCatImageBinding

class HomePagingAdapter : PagingDataAdapter<CatImage, HomePagingAdapter.CatImageViewHolder>(DiffCallback) {

    var onImageClick: ((CatImage) -> Unit)? = null
    var onFavoriteClick: ((CatImage, Boolean) -> Unit)? = null
    var onImageLongClick: ((CatImage) -> Unit)? = null
    var onImageVisible: ((CatImage) -> Unit)? = null

    // Кэш для хранения состояния избранного (на время сессии)
    private val favoriteStateCache = mutableMapOf<String, Boolean>()
    private val viewedImages = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatImageViewHolder {
        val binding = ItemCatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatImageViewHolder, position: Int) {
        val catImage = getItem(position)
        catImage?.let { image ->
            // Используем кэшированное состояние если есть, иначе из данных
            val isFavorite = favoriteStateCache[image.id] ?: image.isInFavorites
            val imageToShow = image.copy(isInFavorites = isFavorite)
            holder.bind(imageToShow)

            // ОБНОВЛЯЕМ ВРЕМЯ ПРОСМОТРА ПРИ ПОЯВЛЕНИИ ИЗОБРАЖЕНИЯ НА ЭКРАНЕ
            if (!viewedImages.contains(image.id)) {
                onImageVisible?.invoke(imageToShow)
                viewedImages.add(image.id)
            }
        }
    }

    // Метод для обновления состояния избранного конкретного элемента
    fun updateFavoriteState(imageId: String, isFavorite: Boolean) {
        favoriteStateCache[imageId] = isFavorite
        // Находим позицию элемента и обновляем его
        snapshot().items.forEachIndexed { index, catImage ->
            if (catImage.id == imageId) {
                notifyItemChanged(index)
                return
            }
        }
    }

    // Очищаем кэш при уничтожении адаптера
    fun clearCache() {
        favoriteStateCache.clear()
        viewedImages.clear()
    }

    inner class CatImageViewHolder(private val binding: ItemCatImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(catImage: CatImage) {
            Glide.with(binding.root)
                .load(catImage.url)
                .centerCrop()
                .into(binding.imageView)

            updateFavoriteIcon(catImage.isInFavorites)

            // Обработчик клика по кнопке избранного
            binding.favoriteButton.setOnClickListener {
                val newFavoriteState = !catImage.isInFavorites
                onFavoriteClick?.invoke(catImage, newFavoriteState)
                updateFavoriteIcon(newFavoriteState)

                // Сохраняем состояние в кэш
                favoriteStateCache[catImage.id] = newFavoriteState

                val message = if (newFavoriteState) "Добавлено в избранное! ❤️" else "Удалено из избранного"
                showToast(message)
            }

            binding.imageView.setOnClickListener {
                onImageClick?.invoke(catImage)
            }

            // Долгое нажатие для быстрого удаления
            binding.imageView.setOnLongClickListener {
                onImageLongClick?.invoke(catImage)
                true
            }

            binding.favoriteButton.setOnLongClickListener {
                onImageLongClick?.invoke(catImage)
                true
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            val icon = if (isFavorite) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_border
            }
            binding.favoriteButton.setImageResource(icon)

            binding.favoriteButton.contentDescription =
                if (isFavorite) "Удалить из избранного" else "Добавить в избранное"
        }

        private fun showToast(message: String) {
            android.widget.Toast.makeText(binding.root.context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CatImage>() {
        override fun areItemsTheSame(oldItem: CatImage, newItem: CatImage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CatImage, newItem: CatImage): Boolean {
            return oldItem == newItem
        }
    }
}