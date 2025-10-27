package com.example.funny_cats.ui.breeds

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.funny_cats.R
import com.example.funny_cats.data.model.CatBreed
import com.example.funny_cats.databinding.ItemBreedBinding

class BreedsAdapter(
    private val onItemClick: (CatBreed) -> Unit
) : ListAdapter<CatBreed, BreedsAdapter.BreedsViewHolder>(DiffCallback) {

    inner class BreedsViewHolder(private val binding: ItemBreedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(breed: CatBreed) {
            with(binding) {
                // Загрузка изображения с помощью Glide
                Glide.with(imageViewBreed.context)
                    .load(breed.getImageUrl())
                    .placeholder(R.drawable.ic_cat_placeholder)
                    .error(R.drawable.ic_cat_placeholder)
                    .into(imageViewBreed)

                textViewBreedName.text = breed.name
                textViewBreedOrigin.text = breed.origin ?: "Неизвестно"
                textViewBreedTemperament.text = breed.temperament ?: "Не указан"

                root.setOnClickListener {
                    onItemClick(breed)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreedsViewHolder {
        val binding = ItemBreedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BreedsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BreedsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CatBreed>() {
        override fun areItemsTheSame(oldItem: CatBreed, newItem: CatBreed): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CatBreed, newItem: CatBreed): Boolean {
            return oldItem == newItem
        }
    }
}