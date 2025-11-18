package com.example.funny_cats.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.funny_cats.R
import com.example.funny_cats.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: HomePagingAdapter

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
        setupPagingObservers()
        setupFavoriteUpdatesObserver()

        binding.textHome.text = "🐱 Загружаем котиков..."
        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun setupRecyclerView() {
        adapter = HomePagingAdapter()

        adapter.onImageClick = { catImage ->
            // ТЕПЕРЬ ОБНОВЛЯЕМ ВРЕМЯ ПРОСМОТРА ТОЛЬКО ПРИ КЛИКЕ (переходе в детали)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.updateViewTime(catImage.id)
            }

            val bundle = Bundle().apply {
                putString("image_url", catImage.url)
                putString("image_id", catImage.id)
            }
            findNavController().navigate(R.id.imageDetailFragment, bundle)
        }

        adapter.onFavoriteClick = { catImage, isFavorite ->
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.toggleImageFavorite(catImage.id, isFavorite)
            }
        }

        adapter.onImageLongClick = { catImage ->
            if (catImage.isInFavorites) {
                showDeleteConfirmationDialog(catImage)
            } else {
                showToast("Нажмите на сердечко, чтобы добавить в избранное")
            }
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter
    }


    // Наблюдатель за изменениями избранного
    private fun setupFavoriteUpdatesObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteUpdates.collect { (imageId, isFavorite) ->
                // Обновляем конкретный элемент в адаптере
                adapter.updateFavoriteState(imageId, isFavorite)
            }
        }
    }

    private fun showDeleteConfirmationDialog(catImage: com.example.funny_cats.data.local.model.CatImage) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удалить из избранного")
            .setMessage("Вы уверены, что хотите удалить этого котика из избранного?")
            .setPositiveButton("Удалить") { dialog, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.toggleImageFavorite(catImage.id, false)
                    showToast("Удалено из избранного")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun setupPagingObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.catsPagingFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

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

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем binding для предотвращения утечек памяти
        _binding = null
        // Очищаем кэш адаптера
        adapter.clearCache()
    }
}