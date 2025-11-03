package com.example.funny_cats.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.funny_cats.R
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.databinding.ItemCatImageBinding

class HomePagingAdapter : PagingDataAdapter<CatImage, HomePagingAdapter.CatImageViewHolder>(DiffCallback) {

    var onImageClick: ((CatImage) -> Unit)? = null
    var onFavoriteClick: ((CatImage, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatImageViewHolder {
        val binding = ItemCatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatImageViewHolder, position: Int) {
        val catImage = getItem(position)
        holder.bind(catImage)
    }

    // Метод для обновления состояния избранного в определенной позиции
    fun updateFavoriteState(imageId: String, isFavorite: Boolean) {
        snapshot().items.forEachIndexed { index, catImage ->
            if (catImage.id == imageId) {
                val updatedImage = catImage.copy(isInFavorites = isFavorite)
                // Обновляем элемент в списке
                notifyItemChanged(index)
                return
            }
        }
    }

    inner class CatImageViewHolder(private val binding: ItemCatImageBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(catImage: CatImage?) {
            catImage?.let { image ->
                Glide.with(binding.root)
                    .load(image.url)
                    .centerCrop()
                    .into(binding.imageView)

                updateFavoriteIcon(image.isInFavorites)

                binding.favoriteButton.setOnClickListener {
                    val newFavoriteState = !image.isInFavorites
                    onFavoriteClick?.invoke(image, newFavoriteState)
                    updateFavoriteIcon(newFavoriteState)
                }

                binding.imageView.setOnClickListener {
                    onImageClick?.invoke(image)
                }
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

    companion object DiffCallback : DiffUtil.ItemCallback<CatImage>() {
        override fun areItemsTheSame(oldItem: CatImage, newItem: CatImage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CatImage, newItem: CatImage): Boolean {
            return oldItem == newItem
        }
    }
}