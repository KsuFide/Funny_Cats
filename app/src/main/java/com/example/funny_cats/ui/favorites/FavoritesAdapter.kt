package com.example.funny_cats.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.funny_cats.databinding.ItemFavoriteBinding

class FavoritesAdapter(
    private val favorites: List<FavoriteItem>,
    private val onItemClick: (FavoriteItem) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount(): Int = favorites.size

    inner class FavoriteViewHolder(
        private val binding: ItemFavoriteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: FavoriteItem) {
            binding.apply {
                // Временная заглушка для изображения
                catImage.setImageResource(android.R.drawable.ic_menu_gallery)

                breedName.text = favorite.name

                // Обработчик клика
                root.setOnClickListener {
                    onItemClick(favorite)
                }

                // Кнопка удаления из избранного
                removeButton.setOnClickListener {
                    // TODO: Реализовать удаление из избранного
                }
            }
        }
    }
}

data class FavoriteItem(
    val name: String,
    val imageUrl: String,
    val breedId: String
)