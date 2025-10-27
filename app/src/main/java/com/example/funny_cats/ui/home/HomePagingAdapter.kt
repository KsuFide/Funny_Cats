package com.example.funny_cats.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.databinding.ItemCatImageBinding

class HomePagingAdapter : PagingDataAdapter<CatImage, HomePagingAdapter.CatImageViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatImageViewHolder {
        val binding = ItemCatImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatImageViewHolder, position: Int) {
        val catImage = getItem(position)
        holder.bind(catImage)
    }

    class CatImageViewHolder(private val binding: ItemCatImageBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(catImage: CatImage?) {
            catImage?.let {
                Glide.with(binding.root)
                    .load(it.url)
                    .centerCrop()
                    .into(binding.imageView)
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