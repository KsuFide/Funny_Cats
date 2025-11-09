package com.example.funny_cats.ui.breeds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.databinding.ItemCatImageBinding

class BreedImagesAdapter : ListAdapter<CatImage, BreedImagesAdapter.BreedImageViewHolder>(DiffCallback) {

    var onImageClick: ((CatImage) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreedImageViewHolder {
        val binding = ItemCatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BreedImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BreedImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BreedImageViewHolder(private val binding: ItemCatImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(catImage: CatImage) {
            Glide.with(binding.root)
                .load(catImage.url)
                .centerCrop()
                .into(binding.imageView)

            // Полностью скрываем кнопку избранного в деталях пород
            binding.favoriteButton.visibility = View.GONE

            binding.imageView.setOnClickListener {
                onImageClick?.invoke(catImage)
            }
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