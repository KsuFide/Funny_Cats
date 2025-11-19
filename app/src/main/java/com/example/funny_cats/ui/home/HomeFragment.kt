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
import com.example.funny_cats.ui.BaseFragment
import com.example.funny_cats.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

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

        Logger.d("HomeFragment onViewCreated")
        setupRecyclerView()
        setupPagingObservers()
        setupFavoriteUpdatesObserver()

        binding.textHome.text = "🐱 Загружаем котиков..."
        binding.swipeRefreshLayout.setOnRefreshListener {
            Logger.d("Manual refresh triggered")
            adapter.refresh()
        }
    }

    private fun setupRecyclerView() {
        adapter = HomePagingAdapter()
        Logger.d("Setting up RecyclerView with adapter")

        adapter.onImageClick = { catImage ->
            Logger.d("Image clicked: ${catImage.id}")
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
            Logger.d("Favorite clicked for image: ${catImage.id}, isFavorite: $isFavorite")
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.toggleImageFavorite(catImage.id, isFavorite)
            }
        }

        adapter.onImageLongClick = { catImage ->
            Logger.d("Image long clicked: ${catImage.id}")
            if (catImage.isInFavorites) {
                showDeleteConfirmationDialog(catImage)
            } else {
                showToast("Нажмите на сердечко, чтобы добавить в избранное")
            }
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Logger.v("RecyclerView scrolling stopped")
                }
            }
        })
    }

    private fun setupFavoriteUpdatesObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteUpdates.collect { (imageId, isFavorite) ->
                Logger.d("Favorite update received: $imageId -> $isFavorite")
                adapter.updateFavoriteState(imageId, isFavorite)
            }
        }
    }

    private fun showDeleteConfirmationDialog(catImage: com.example.funny_cats.data.local.model.CatImage) {
        Logger.d("Showing delete confirmation dialog for image: ${catImage.id}")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удалить из избранного")
            .setMessage("Вы уверены, что хотите удалить этого котика из избранного?")
            .setPositiveButton("Удалить") { dialog, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.toggleImageFavorite(catImage.id, false)
                    showToast("Удалено из избранное")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun setupPagingObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.catsPagingFlow.collectLatest { pagingData ->
                Logger.d("New paging data received: ${pagingData.toString().take(50)}...")
                adapter.submitData(pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                binding.swipeRefreshLayout.isRefreshing = loadState.refresh is androidx.paging.LoadState.Loading

                when (loadState.refresh) {
                    is androidx.paging.LoadState.Loading -> {
                        Logger.d("Loading state: Loading")
                        binding.textHome.text = "🐱 Загружаем котиков..."
                        binding.textHome.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                    is androidx.paging.LoadState.NotLoading -> {
                        Logger.d("Loading state: NotLoading, item count: ${adapter.itemCount}")
                        binding.textHome.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                    is androidx.paging.LoadState.Error -> {
                        val errorState = loadState.refresh as androidx.paging.LoadState.Error
                        Logger.e("Loading state: Error - ${errorState.error.message}", errorState.error)

                        binding.textHome.text = "😿 Проблемы с загрузкой котиков\n\nПопробуйте позже или проверьте интернет"
                        binding.textHome.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        try {
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
            Logger.d("Toast shown: $message")
        } catch (e: Exception) {
            Logger.e("Failed to show toast: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Logger.d("HomeFragment onDestroyView")

        try {
            _binding?.recyclerView?.adapter = null
            adapter.clearCache()
            _binding = null
            Logger.d("HomeFragment binding cleared successfully")
        } catch (e: Exception) {
            Logger.e("Error clearing HomeFragment binding: ${e.message}", e)
        }
    }
}