package com.example.funny_cats.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funny_cats.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val TAG = "HomeFragment"

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: CatImageAdapter

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
        setupObservers()

        binding.textHome.text = "🐱 Загружаем котиков..."
        loadRandomCats()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadRandomCats()
        }
    }

    private fun setupRecyclerView() {
        adapter = CatImageAdapter()
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.catImages.collect { images ->
                if (images.isNotEmpty()) {
                    adapter.submitList(images)
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

    private fun loadRandomCats() {
        viewModel.loadRandomCats(limit = 10)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}