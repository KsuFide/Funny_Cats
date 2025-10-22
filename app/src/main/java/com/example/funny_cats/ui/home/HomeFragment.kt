package com.example.funny_cats.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funny_cats.databinding.FragmentHomeBinding
import com.example.funny_cats.data.api.RetrofitInstance
import kotlinx.coroutines.launch
import android.util.Log

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val TAG = "HomeFragment"

    // Временный адаптер (позже заменим на нормальный)
    private val catImages = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadRandomCats()

        // Добавим кнопку обновления
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadRandomCats()
        }
    }

    private fun setupRecyclerView() {
        // Временно просто покажем текст, что данные загружаются
        binding.textHome.text = "🐱 Загружаем котиков..."
    }

    private fun loadRandomCats() {
        binding.swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch {
            try {
                Log.d(TAG, "🔄 Загружаем случайных котиков...")
                val response = RetrofitInstance.api.getRandomCats(limit = 10)

                if (response.isNotEmpty()) {
                    // Сохраняем URLы картинок
                    catImages.clear()
                    catImages.addAll(response.map { it.url })

                    // Покажем первую картинку в тексте (временно)
                    binding.textHome.text = "🐱 Загружено ${response.size} котиков!\n\n" +
                            "Первая картинка: ${response[0].url}\n\n" +
                            "Размер: ${response[0].width}x${response[0].height}"

                    Log.d(TAG, "✅ Успешно загружено ${response.size} котиков")
                } else {
                    binding.textHome.text = "😿 Котики куда-то пропали...\nПопробуйте обновить"
                    Log.w(TAG, "⚠️ API вернуло пустой список")
                }

            } catch (e: Exception) {
                binding.textHome.text = "❌ Ошибка загрузки:\n${e.message}\n\nПроверьте интернет"
                Log.e(TAG, "❌ Ошибка при загрузке котиков: ${e.message}", e)
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}