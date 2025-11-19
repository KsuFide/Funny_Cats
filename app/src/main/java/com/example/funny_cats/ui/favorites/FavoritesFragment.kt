package com.example.funny_cats.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funny_cats.R
import com.example.funny_cats.databinding.FragmentFavoritesBinding
import com.example.funny_cats.ui.BaseFragment
import com.example.funny_cats.ui.home.HomePagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : BaseFragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var adapter: HomePagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        adapter = HomePagingAdapter()

        adapter.onImageClick = { catImage ->
            val bundle = Bundle().apply {
                putString("image_url", catImage.url)
                putString("image_id", catImage.id)
            }
            findNavController().navigate(R.id.imageDetailFragment, bundle)
        }

        adapter.onFavoriteClick = { catImage, isFavorite ->
            viewLifecycleOwner.lifecycleScope.launch {
                if (!isFavorite) {
                    viewModel.toggleImageFavorite(catImage.id, false)
                    showToast("Удалено из избранного")
                    // Обновляем список после небольшой задержки для обновления БД
                    viewLifecycleOwner.lifecycleScope.launch {
                        kotlinx.coroutines.delay(500)
                        adapter.refresh()
                    }
                }
            }
        }

        adapter.onImageLongClick = { catImage ->
            showDeleteConfirmationDialog(catImage)
        }

        binding.recyclerViewFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewFavorites.adapter = adapter
    }

    private fun showDeleteConfirmationDialog(catImage: com.example.funny_cats.data.local.model.CatImage) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удалить из избранного")
            .setMessage("Вы уверены, что хотите удалить этого котика из избранного?")
            .setPositiveButton("Удалить") { dialog, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.toggleImageFavorite(catImage.id, false)
                    showToast("Удалено из избранного")
                    // Обновляем список после удаления
                    viewLifecycleOwner.lifecycleScope.launch {
                        kotlinx.coroutines.delay(500)
                        adapter.refresh()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteImagesPaging.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                binding.swipeRefresh.isRefreshing = loadState.refresh is androidx.paging.LoadState.Loading

                val isEmpty = loadState.refresh is androidx.paging.LoadState.NotLoading &&
                        adapter.itemCount == 0

                if (isEmpty) {
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.textEmpty.text = "Пока нет избранных котиков\n\n❤️ Нажмите на сердечко у фото, чтобы добавить котика в избранное!"
                    binding.recyclerViewFavorites.visibility = View.GONE
                } else {
                    binding.textEmpty.visibility = View.GONE
                    binding.recyclerViewFavorites.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при каждом открытии фрагмента
        adapter.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.recyclerViewFavorites?.adapter = null
        _binding = null
    }
}