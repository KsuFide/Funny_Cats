package com.example.funny_cats.ui.breeds

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.funny_cats.R
import com.example.funny_cats.data.local.model.CatBreed
import com.example.funny_cats.databinding.ItemBreedBinding

class BreedsAdapter(
    private val onItemClick: (CatBreed) -> Unit
) : PagingDataAdapter<CatBreed, BreedsAdapter.BreedsViewHolder>(DiffCallback) {

    inner class BreedsViewHolder(private val binding: ItemBreedBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(breed: CatBreed?) {
            breed?.let { catBreed ->
                with(binding) {
                    // Загрузка изображения
                    Glide.with(imageViewBreed.context)
                        .load(catBreed.getImageUrl())
                        .placeholder(R.drawable.ic_cat_placeholder)
                        .error(R.drawable.ic_cat_placeholder)
                        .into(imageViewBreed)

                    textViewBreedName.text = catBreed.name
                    textViewBreedOrigin.text = catBreed.origin ?: "Неизвестно"
                    textViewBreedTemperament.text = catBreed.temperament ?: "Не указан"

                    root.setOnClickListener {
                        onItemClick(catBreed)
                    }
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