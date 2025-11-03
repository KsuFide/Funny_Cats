package com.example.funny_cats.ui.history

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
import com.example.funny_cats.databinding.FragmentHistoryBinding
import com.example.funny_cats.ui.home.HomePagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HomePagingAdapter

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
                viewModel.toggleImageFavorite(catImage.id, isFavorite)
                val message = if (isFavorite) "Добавлено в избранное! ❤️" else "Удалено из избранного"
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        binding.recyclerViewHistory.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewHistory.adapter = adapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historyImagesPaging.collectLatest { pagingData ->
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
                    binding.textEmpty.text = "История просмотров пуста\n\n📸 Смотрите котиков в главной ленте, и они появятся здесь!"
                    binding.recyclerViewHistory.visibility = View.GONE
                } else {
                    binding.textEmpty.visibility = View.GONE
                    binding.recyclerViewHistory.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем список при каждом открытии экрана
        adapter.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}