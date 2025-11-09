package com.example.funny_cats.ui.breeds

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.funny_cats.R
import com.example.funny_cats.data.local.model.CatBreed
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.data.repository.toCatBreed
import com.example.funny_cats.databinding.ItemBreedBinding
import com.example.funny_cats.util.RussianTranslator

class BreedsAdapter(
    private val onItemClick: (CatBreed) -> Unit
) : PagingDataAdapter<CatBreedEntity, BreedsAdapter.BreedsViewHolder>(DiffCallback) {

    inner class BreedsViewHolder(private val binding: ItemBreedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(breed: CatBreedEntity?) {
            breed?.let { catBreed ->
                with(binding) {
                    // Загрузка изображения
                    Glide.with(imageViewBreed.context)
                        .load(catBreed.imageId?.let { "https://cdn2.thecatapi.com/images/$it.jpg" })
                        .placeholder(R.drawable.ic_cat_placeholder)
                        .error(R.drawable.ic_cat_placeholder)
                        .into(imageViewBreed)

                    // Русское название породы
                    textViewBreedName.text = RussianTranslator.translateBreedName(catBreed.name)

                    // Русское происхождение
                    textViewBreedOrigin.text = RussianTranslator.translateOrigin(catBreed.origin)

                    // Русский темперамент (сокращенный)
                    textViewBreedTemperament.text = RussianTranslator.getShortTemperament(catBreed.temperament)

                    // Обработчик клика с безопасностью
                    root.setOnClickListener {
                        try {
                            onItemClick(catBreed.toCatBreed())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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

    companion object DiffCallback : DiffUtil.ItemCallback<CatBreedEntity>() {
        override fun areItemsTheSame(oldItem: CatBreedEntity, newItem: CatBreedEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CatBreedEntity, newItem: CatBreedEntity): Boolean {
            return oldItem == newItem
        }
    }
}