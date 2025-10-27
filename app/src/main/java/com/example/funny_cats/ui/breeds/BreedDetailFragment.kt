package com.example.funny_cats.ui.breeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.model.CatBreed
import com.example.funny_cats.databinding.FragmentBreedDetailBinding
import com.example.funny_cats.ui.home.CatImageAdapter
import kotlinx.coroutines.launch

class BreedDetailFragment : Fragment() {

    private var _binding: FragmentBreedDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CatImageAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBreedDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val breedId = arguments?.getString("breedId")
        breedId?.let { loadBreedDetails(it) }

        setupImageRecyclerView()
    }

    private fun setupImageRecyclerView() {
        adapter = CatImageAdapter()
        binding.recyclerViewBreedImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@BreedDetailFragment.adapter
        }
    }

    private fun loadBreedDetails(breedId: String) {
        lifecycleScope.launch {
            try {
                // Загружаем информацию о породе
                val breeds = RetrofitInstance.api.getAllBreeds()
                val breed = breeds.find { it.id == breedId }

                breed?.let {
                    displayBreedInfo(it)
                    loadBreedImages(it.id)
                }
            } catch (e: Exception) {
                binding.textBreedDetail.text = "❌ Ошибка загрузки информации о породе"
            }
        }
    }

    private fun displayBreedInfo(breed: CatBreed) {
        val stringBuilder = StringBuilder()

        stringBuilder.appendLine("🐱 ${breed.name}")
        stringBuilder.appendLine()

        breed.description?.let {
            stringBuilder.appendLine("📖 $it")
            stringBuilder.appendLine()
        }

        breed.temperament?.let {
            stringBuilder.appendLine("🎯 Темперамент: $it")
            stringBuilder.appendLine()
        }

        breed.origin?.let {
            stringBuilder.appendLine("🌍 Происхождение: $it")
            stringBuilder.appendLine()
        }

        // Добавляем поля только если они есть
        breed.lifeSpan?.let {
            stringBuilder.appendLine("⏳ Продолжительность жизни: $it лет")
        }

        breed.intelligence?.let {
            stringBuilder.appendLine("💡 Интеллект: $it/5")
        }

        breed.dogFriendly?.let {
            stringBuilder.appendLine("🐶 Дружелюбность к собакам: $it/5")
        }

        breed.adaptability?.let {
            stringBuilder.appendLine("🔧 Адаптивность: $it/5")
        }

        breed.wikipediaUrl?.let {
            stringBuilder.appendLine()
            stringBuilder.appendLine("🔗 Wikipedia: $it")
        }

        binding.textBreedDetail.text = stringBuilder.toString()
    }

    private fun loadBreedImages(breedId: String) {
        lifecycleScope.launch {
            try {
                val images = RetrofitInstance.api.getBreedImages(breedId, limit = 8)
                adapter.submitList(images)
            } catch (e: Exception) {
                // Обработка ошибок
                binding.textBreedDetail.append("\n\n❌ Не удалось загрузить изображения")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}