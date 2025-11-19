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
import com.example.funny_cats.ui.BaseFragment
import com.example.funny_cats.ui.breeds.BreedsAdapter
import com.example.funny_cats.ui.home.HomePagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : BaseFragment() {

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

        showAllHistory()
    }

    private fun setupAdapters() {
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

        breedAdapter = BreedsAdapter { breed ->
            val bundle = Bundle().apply {
                putString("breedId", breed.id)
            }
            findNavController().navigate(R.id.breedDetailFragment, bundle)
        }

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
        if (!isAdded || isDetached) return

        try {
            updateTabSelection(binding.tabAll)
            binding.recyclerViewHistory.adapter = combinedAdapter

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.combinedHistory.collect { historyItems ->
                        if (isAdded) {
                            combinedAdapter.submitList(historyItems)
                            updateEmptyState(historyItems.isEmpty(), "всех элементов")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (isAdded) {
                        updateEmptyState(true, "всех элементов")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showBreedHistory() {
        if (!isAdded || isDetached) return

        updateTabSelection(binding.tabBreeds)
        binding.recyclerViewHistory.adapter = breedAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewedBreedsPaging.collectLatest { pagingData ->
                if (isAdded) {
                    breedAdapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            breedAdapter.loadStateFlow.collect { loadState ->
                if (isAdded) {
                    val isEmpty = loadState.refresh is androidx.paging.LoadState.NotLoading &&
                            breedAdapter.itemCount == 0
                    updateEmptyState(isEmpty, "пород")
                }
            }
        }
    }

    private fun showImageHistory() {
        if (!isAdded || isDetached) return

        updateTabSelection(binding.tabImages)
        binding.recyclerViewHistory.adapter = imageAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewedImagesPaging.collectLatest { pagingData ->
                if (isAdded) {
                    imageAdapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            imageAdapter.loadStateFlow.collect { loadState ->
                if (isAdded) {
                    val isEmpty = loadState.refresh is androidx.paging.LoadState.NotLoading &&
                            imageAdapter.itemCount == 0
                    updateEmptyState(isEmpty, "изображений")
                }
            }
        }
    }

    private fun updateTabSelection(selectedTab: View) {
        if (!isAdded) return

        binding.tabAll.isSelected = false
        binding.tabBreeds.isSelected = false
        binding.tabImages.isSelected = false
        selectedTab.isSelected = true
    }

    private fun observeData() {
        // Дополнительная логика наблюдения если нужна
    }

    private fun updateEmptyState(isEmpty: Boolean, type: String) {
        if (!isAdded || isDetached) {
            return
        }

        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            if (!isAdded) return@setOnRefreshListener

            when {
                binding.tabAll.isSelected -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.combinedHistory.collect { historyItems ->
                            if (isAdded) {
                                combinedAdapter.submitList(historyItems)
                                binding.swipeRefresh.isRefreshing = false
                            }
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
        if (!isAdded) return

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

                    if (isAdded) {
                        when {
                            binding.tabAll.isSelected -> showAllHistory()
                            binding.tabBreeds.isSelected -> breedAdapter.refresh()
                            binding.tabImages.isSelected -> imageAdapter.refresh()
                        }
                        showToast("История очищена")
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showToast(message: String) {
        if (!isAdded) return
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (!isAdded) return

        when {
            binding.tabAll.isSelected -> showAllHistory()
            binding.tabBreeds.isSelected -> breedAdapter.refresh()
            binding.tabImages.isSelected -> imageAdapter.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Очищаем адаптеры
        _binding?.recyclerViewHistory?.adapter = null

        // Очищаем ресурсы адаптеров
        if (::combinedAdapter.isInitialized) {
            combinedAdapter.clearResources()
        }
        if (::imageAdapter.isInitialized) {
            imageAdapter.clearCache()
        }

        _binding = null
    }
}