package com.example.funny_cats.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.funny_cats.data.model.CatImage
import com.example.funny_cats.databinding.ItemCatImageBinding

class CatImageAdapter : ListAdapter<CatImage, CatImageAdapter.CatImageViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatImageViewHolder {
        val binding = ItemCatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatImageViewHolder, position: Int) {
        val catImage = getItem(position)
        holder.bind(catImage)
    }

    class CatImageViewHolder(private val binding: ItemCatImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(catImage: CatImage) {
            Glide.with(binding.root)
                .load(catImage.url)
                .centerCrop()
                .into(binding.imageView)
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