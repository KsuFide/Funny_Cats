package com.example.funny_cats.ui.breeds

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funny_cats.R
import com.example.funny_cats.data.api.RetrofitInstance
import com.example.funny_cats.data.local.model.CatBreed
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.databinding.FragmentBreedDetailBinding
import com.example.funny_cats.ui.history.BreedHistoryViewModel
import com.example.funny_cats.util.RussianTranslator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.util.Log
import com.example.funny_cats.data.repository.toCatBreed
import com.example.funny_cats.ui.BaseFragment

@AndroidEntryPoint
class BreedDetailFragment : BaseFragment() {

    private var _binding: FragmentBreedDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BreedImagesAdapter
    private var currentBreedId: String? = null
    private val breedHistoryViewModel: BreedHistoryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBreedDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentBreedId = arguments?.getString("breedId")

        if (currentBreedId.isNullOrEmpty()) {
            showError("Не удалось загрузить информацию о породе")
            return
        }

        setupImageRecyclerView()
        loadBreedDetails(currentBreedId!!)
    }

    private fun setupImageRecyclerView() {
        adapter = BreedImagesAdapter()

        adapter.onImageClick = { catImage ->
            try {
                val bundle = Bundle().apply {
                    putString("image_url", catImage.url)
                    putString("image_id", catImage.id)
                    putString("breed_id", currentBreedId)
                }
                findNavController().navigate(R.id.imageDetailFragment, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Ошибка перехода к изображению")
            }
        }

        binding.recyclerViewBreedImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@BreedDetailFragment.adapter
        }
    }

    private fun loadBreedDetails(breedId: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.textBreedDetail.text = "🐱 Загружаем информацию о породе..."

                val breeds = try {
                    RetrofitInstance.api.getAllBreeds()
                } catch (e: Exception) {
                    Log.e("BreedDetail", "Failed to load breeds: ${e.message}")
                    // Пробуем получить породу из базы данных
                    val breedFromDb = breedHistoryViewModel.getBreedById(breedId)
                    if (breedFromDb != null) {
                        listOf(breedFromDb.toCatBreed())
                    } else {
                        emptyList()
                    }
                }

                val breed = breeds.find { it.id == breedId }

                breed?.let {
                    saveBreedAndUpdateHistory(it)
                    displayBreedInfo(it)
                    loadBreedImages(it.id)
                } ?: run {
                    binding.textBreedDetail.text = "❌ Порода не найдена\n\nПопробуйте обновить список пород"
                    binding.progressBar.visibility = View.GONE
                }

            } catch (e: Exception) {
                e.printStackTrace()
                binding.textBreedDetail.text = "❌ Ошибка загрузки информации о породе\n\nПроверьте подключение к интернету"
                binding.progressBar.visibility = View.GONE
                showToast("Проблемы с загрузкой данных")
            }
        }
    }

    private suspend fun saveBreedAndUpdateHistory(breed: CatBreed) {
        try {
            breedHistoryViewModel.saveBreedToDatabase(breed)
            kotlinx.coroutines.delay(100)
            breedHistoryViewModel.updateBreedViewTime(breed.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadBreedImages(breedId: String) {
        lifecycleScope.launch {
            try {
                val imagesFromApi = try {
                    RetrofitInstance.api.getBreedImages(breedId, limit = 8)
                } catch (e: Exception) {
                    Log.e("BreedDetail", "Failed to load breed images: ${e.message}")
                    // Возвращаем пустой список при ошибке
                    emptyList<CatImage>()
                }

                if (imagesFromApi.isNotEmpty()) {
                    adapter.submitList(imagesFromApi)
                    binding.textImagesTitle.visibility = View.VISIBLE
                    binding.recyclerViewBreedImages.visibility = View.VISIBLE
                } else {
                    binding.textImagesTitle.visibility = View.GONE
                    binding.recyclerViewBreedImages.visibility = View.GONE
                    binding.textBreedDetail.append("\n\n📷 Изображения для этой породы не найдены или временно недоступны")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.textBreedDetail.append("\n\n❌ Не удалось загрузить изображения")
                binding.textImagesTitle.visibility = View.GONE
                binding.recyclerViewBreedImages.visibility = View.GONE
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayBreedInfo(breed: CatBreed) {
        val stringBuilder = StringBuilder()

        val russianName = RussianTranslator.translateBreedName(breed.name)
        stringBuilder.appendLine("🐱 $russianName")
        stringBuilder.appendLine()

        breed.temperament?.let { temperament ->
            val russianTemperament = RussianTranslator.translateTemperament(temperament)
            stringBuilder.appendLine("🎯 Темперамент: $russianTemperament")
            stringBuilder.appendLine()
        }

        breed.origin?.let { origin ->
            val russianOrigin = RussianTranslator.translateOrigin(origin)
            stringBuilder.appendLine("🌍 Происхождение: $russianOrigin")
            stringBuilder.appendLine()
        }

        breed.lifeSpan?.let {
            stringBuilder.appendLine("⏳ Продолжительность жизни: $it лет")
            stringBuilder.appendLine()
        }

        breed.intelligence?.let {
            stringBuilder.appendLine("💡 Интеллект: ${getStars(it)}")
        }

        breed.dogFriendly?.let {
            stringBuilder.appendLine("🐶 Дружелюбность к собакам: ${getStars(it)}")
        }

        breed.adaptability?.let {
            stringBuilder.appendLine("🔧 Адаптивность: ${getStars(it)}")
        }

        binding.textBreedDetail.text = stringBuilder.toString()

        breed.wikipediaUrl?.let { url ->
            val linkTextView = TextView(requireContext()).apply {
                text = "🔗 Подробнее на Wikipedia"
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
                paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener {
                    openWikipedia(url)
                }
                setPadding(0, 16, 0, 0)
            }

            (binding.root as? LinearLayout)?.addView(linkTextView)
        }
    }

    private fun openWikipedia(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Не удалось открыть ссылку")
        }
    }

    private fun getStars(rating: Int): String {
        val filledStars = "★".repeat(rating)
        val emptyStars = "☆".repeat(5 - rating)
        return "$filledStars$emptyStars ($rating/5)"
    }

    private fun showError(message: String) {
        binding.textBreedDetail.text = message
        binding.progressBar.visibility = View.GONE
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.recyclerViewBreedImages?.adapter = null
        _binding = null
    }
}