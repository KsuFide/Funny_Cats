package com.example.funny_cats.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.funny_cats.R
import com.example.funny_cats.databinding.FragmentHistoryBinding
import com.example.funny_cats.ui.breeds.BreedsAdapter
import com.example.funny_cats.ui.home.HomePagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()

    private lateinit var combinedAdapter: CombinedHistoryAdapter
    private lateinit var breedAdapter: BreedsAdapter
    private lateinit var imageAdapter: HomePagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupTabs()
        observeData()
        setupSwipeRefresh()
        setupClearHistoryButton()

        // По умолчанию показываем все
        showAllHistory()
    }

    private fun setupAdapters() {
        // Адаптер для объединенной истории
        combinedAdapter = CombinedHistoryAdapter(
            onBreedClick = { breed ->
                val bundle = Bundle().apply {
                    putString("breedId", breed.id)
                }
                findNavController().navigate(R.id.breedDetailFragment, bundle)
            },
            onImageClick = { image ->
                val bundle = Bundle().apply {
                    putString("image_url", image.url)
                    putString("image_id", image.id)
                }
                findNavController().navigate(R.id.imageDetailFragment, bundle)
            },
            onFavoriteClick = { imageId, isFavorite ->
                viewModel.toggleImageFavorite(imageId, isFavorite)
            }
        )

        // Адаптер для пород
        breedAdapter = BreedsAdapter { breed ->
            val bundle = Bundle().apply {
                putString("breedId", breed.id)
            }
            findNavController().navigate(R.id.breedDetailFragment, bundle)
        }

        // Адаптер для изображений
        imageAdapter = HomePagingAdapter()
        imageAdapter.onImageClick = { catImage ->
            val bundle = Bundle().apply {
                putString("image_url", catImage.url)
                putString("image_id", catImage.id)
            }
            findNavController().navigate(R.id.imageDetailFragment, bundle)
        }

        imageAdapter.onFavoriteClick = { catImage, isFavorite ->
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.toggleImageFavorite(catImage.id, isFavorite)
            }
        }

        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupTabs() {
        binding.tabAll.setOnClickListener {
            showAllHistory()
        }

        binding.tabBreeds.setOnClickListener {
            showBreedHistory()
        }

        binding.tabImages.setOnClickListener {
            showImageHistory()
        }
    }

    private fun showAllHistory() {
        updateTabSelection(binding.tabAll)
        binding.recyclerViewHistory.adapter = combinedAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.combinedHistory.collect { historyItems ->
                combinedAdapter.submitList(historyItems)
                updateEmptyState(historyItems.isEmpty(), "всех элементов")
            }
        }
    }

    private fun showBreedHistory() {
        updateTabSelection(binding.tabBreeds)
        binding.recyclerViewHistory.adapter = breedAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewedBreedsPaging.collectLatest { pagingData ->
                breedAdapter.submitData(pagingData)
            }
        }

        // Обновляем состояние пустого списка для пород
        viewLifecycleOwner.lifecycleScope.launch {
            breedAdapter.loadStateFlow.collect { loadState ->
                val isEmpty = loadState.refresh is androidx.paging.LoadState.NotLoading &&
                        breedAdapter.itemCount == 0
                updateEmptyState(isEmpty, "пород")
            }
        }
    }

    private fun showImageHistory() {
        updateTabSelection(binding.tabImages)
        binding.recyclerViewHistory.adapter = imageAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewedImagesPaging.collectLatest { pagingData ->
                imageAdapter.submitData(pagingData)
            }
        }

        // Обновляем состояние пустого списка для изображений
        viewLifecycleOwner.lifecycleScope.launch {
            imageAdapter.loadStateFlow.collect { loadState ->
                val isEmpty = loadState.refresh is androidx.paging.LoadState.NotLoading &&
                        imageAdapter.itemCount == 0
                updateEmptyState(isEmpty, "изображений")
            }
        }
    }

    private fun updateTabSelection(selectedTab: View) {
        // Сбрасываем все табы
        binding.tabAll.isSelected = false
        binding.tabBreeds.isSelected = false
        binding.tabImages.isSelected = false

        // Выделяем выбранный таб
        selectedTab.isSelected = true
    }

    private fun observeData() {
        // Дополнительная логика наблюдения если нужна
    }

    private fun updateEmptyState(isEmpty: Boolean, type: String) {
        if (isEmpty) {
            binding.textEmpty.visibility = View.VISIBLE
            binding.textEmpty.text = "История просмотров $type пуста\n\n📸 Смотрите котиков, и они появятся здесь!"
            binding.recyclerViewHistory.visibility = View.GONE
            binding.buttonClearHistory.visibility = View.GONE
        } else {
            binding.textEmpty.visibility = View.GONE
            binding.recyclerViewHistory.visibility = View.VISIBLE
            binding.buttonClearHistory.visibility = View.VISIBLE
        }

        binding.swipeRefresh.isRefreshing = false
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            when {
                binding.tabAll.isSelected -> {
                    // Для объединенной истории просто обновляем данные
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.combinedHistory.collect { historyItems ->
                            combinedAdapter.submitList(historyItems)
                            binding.swipeRefresh.isRefreshing = false
                        }
                    }
                }
                binding.tabBreeds.isSelected -> breedAdapter.refresh()
                binding.tabImages.isSelected -> imageAdapter.refresh()
            }
        }
    }

    private fun setupClearHistoryButton() {
        binding.buttonClearHistory.setOnClickListener {
            showClearHistoryConfirmation()
        }
    }

    private fun showClearHistoryConfirmation() {
        val message = when {
            binding.tabAll.isSelected -> "Вы уверены, что хотите очистить всю историю просмотров?"
            binding.tabBreeds.isSelected -> "Вы уверены, что хотите очистить историю просмотров пород?"
            binding.tabImages.isSelected -> "Вы уверены, что хотите очистить историю просмотров изображений?"
            else -> "Вы уверены, что хотите очистить историю?"
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Очистить историю")
            .setMessage(message)
            .setPositiveButton("Очистить") { dialog, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    when {
                        binding.tabAll.isSelected -> viewModel.clearAllHistory()
                        binding.tabBreeds.isSelected -> viewModel.clearBreedHistory()
                        binding.tabImages.isSelected -> viewModel.clearImageHistory()
                    }

                    // Обновляем адаптеры
                    when {
                        binding.tabAll.isSelected -> showAllHistory()
                        binding.tabBreeds.isSelected -> breedAdapter.refresh()
                        binding.tabImages.isSelected -> imageAdapter.refresh()
                    }

                    showToast("История очищена")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем списки при каждом открытии экрана
        when {
            binding.tabAll.isSelected -> showAllHistory()
            binding.tabBreeds.isSelected -> breedAdapter.refresh()
            binding.tabImages.isSelected -> imageAdapter.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}