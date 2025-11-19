package com.example.funny_cats.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.funny_cats.R
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.databinding.ItemCatImageBinding

class HomePagingAdapter : PagingDataAdapter<CatImage, HomePagingAdapter.CatImageViewHolder>(DiffCallback) {

    var onImageClick: ((CatImage) -> Unit)? = null
    var onFavoriteClick: ((CatImage, Boolean) -> Unit)? = null
    var onImageLongClick: ((CatImage) -> Unit)? = null

    private val favoriteStateCache = mutableMapOf<String, Boolean>()

    // Оптимизация: кэшируем RequestOptions
    private val glideOptions = RequestOptions()
        .transform(CenterCrop(), RoundedCorners(16))
        .override(300, 300) // Фиксированный размер для оптимизации

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatImageViewHolder {
        val binding = ItemCatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatImageViewHolder, position: Int) {
        val catImage = getItem(position)
        catImage?.let { image ->
            val isFavorite = favoriteStateCache[image.id] ?: image.isInFavorites
            val imageToShow = image.copy(isInFavorites = isFavorite)
            holder.bind(imageToShow)
        }
    }

    fun updateFavoriteState(imageId: String, isFavorite: Boolean) {
        favoriteStateCache[imageId] = isFavorite
        snapshot().items.forEachIndexed { index, catImage ->
            if (catImage.id == imageId) {
                notifyItemChanged(index)
                return
            }
        }
    }

    fun clearCache() {
        favoriteStateCache.clear()
    }

    override fun onViewRecycled(holder: CatImageViewHolder) {
        super.onViewRecycled(holder)
        holder.clearImage()
    }

    inner class CatImageViewHolder(private val binding: ItemCatImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(catImage: CatImage) {
            // Оптимизированная загрузка изображений
            Glide.with(binding.root)
                .load(catImage.url)
                .apply(glideOptions) // Используем кэшированные опции
                .placeholder(R.drawable.ic_cat_placeholder)
                .error(R.drawable.ic_cat_placeholder)
                .into(binding.imageView)

            updateFavoriteIcon(catImage.isInFavorites)

            binding.favoriteButton.setOnClickListener {
                val newFavoriteState = !catImage.isInFavorites
                onFavoriteClick?.invoke(catImage, newFavoriteState)
                updateFavoriteIcon(newFavoriteState)
                favoriteStateCache[catImage.id] = newFavoriteState

                val message = if (newFavoriteState) "Добавлено в избранное! ❤️" else "Удалено из избранного"
                showToast(message)
            }

            binding.imageView.setOnClickListener {
                onImageClick?.invoke(catImage)
            }

            binding.imageView.setOnLongClickListener {
                onImageLongClick?.invoke(catImage)
                true
            }

            binding.favoriteButton.setOnLongClickListener {
                onImageLongClick?.invoke(catImage)
                true
            }
        }

        fun clearImage() {
            // Важно: очищаем Glide при переиспользовании ViewHolder
            Glide.with(binding.root).clear(binding.imageView)
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

    companion object {
        object DiffCallback : DiffUtil.ItemCallback<CatImage>() {
            override fun areItemsTheSame(oldItem: CatImage, newItem: CatImage): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CatImage, newItem: CatImage): Boolean {
                return oldItem == newItem
            }
        }
    }
}