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
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val TAG = "HomeFragment"

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: HomePagingAdapter // Меняем на пагинационный адаптер

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
        setupPagingObservers() // Новая функция для пагинации

        binding.textHome.text = "🐱 Загружаем котиков..."
        loadRandomCatsWithPaging() // Новая функция для загрузки с пагинацией

        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh() // Обновляем данные через адаптер
        }
    }

    private fun setupRecyclerView() {
        adapter = HomePagingAdapter() // Используем пагинационный адаптер
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter
    }

    private fun setupPagingObservers() {
        // Наблюдаем за состоянием загрузки
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                binding.swipeRefreshLayout.isRefreshing = loadState.refresh is androidx.paging.LoadState.Loading

                when (loadState.refresh) {
                    is androidx.paging.LoadState.Loading -> {
                        binding.textHome.text = "🐱 Загружаем котиков..."
                        binding.textHome.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                    is androidx.paging.LoadState.NotLoading -> {
                        binding.textHome.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                    is androidx.paging.LoadState.Error -> {
                        val errorState = loadState.refresh as androidx.paging.LoadState.Error
                        binding.textHome.text = "😿 Не удалось загрузить котиков: ${errorState.error.message}"
                        binding.textHome.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun loadRandomCatsWithPaging() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.catsPagingFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    // Оставляем старый метод для обратной совместимости (можно удалить позже)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.catImages.collect { images ->
                if (images.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.textHome.visibility = View.GONE
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.textHome.visibility = View.VISIBLE
                    binding.textHome.text = "😿 Не удалось загрузить котиков"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefreshLayout.isRefreshing = isLoading
                if (isLoading) {
                    binding.textHome.text = "🐱 Загружаем котиков..."
                }
            }
        }
    }

    // Оставляем старый метод для обратной совместимости (можно удалить позже)
    private fun loadRandomCats() {
        viewModel.loadRandomCats(limit = 10)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}