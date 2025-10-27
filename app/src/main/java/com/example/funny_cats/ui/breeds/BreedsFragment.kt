package com.example.funny_cats.ui.breeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.funny_cats.R
import com.example.funny_cats.databinding.FragmentBreedsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BreedsFragment : Fragment() {

    private var _binding: FragmentBreedsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BreedsViewModel by viewModels()
    private lateinit var adapter: BreedsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBreedsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupObservers()

        println("DEBUG: Fragment created with pagination")
    }

    private fun setupRecyclerView() {
        adapter = BreedsAdapter { breed ->
            // TODO: Переход к деталям породы
            println("DEBUG: Breed clicked: ${breed.name}")
        }
        binding.recyclerViewBreeds.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewBreeds.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.queryHint = getString(R.string.search_hint)
        binding.searchView.setQuery("", false)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                println("DEBUG: Search submitted: $query")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty()
                println("DEBUG: Search text changed: '$query'")
                viewModel.searchBreeds(query)
                return true
            }
        })

        println("DEBUG: SearchView setup completed")
    }

    private fun setupObservers() {
        // Наблюдаем за пагинацией
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.breedsPaging.collectLatest { pagingData ->
                println("DEBUG: New paging data received")
                adapter.submitData(pagingData)
            }
        }

        // Наблюдаем за состоянием загрузки
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                println("DEBUG: Loading state: $isLoading")
                if (isLoading) {
                    binding.textBreeds.text = getString(R.string.loading_breeds)
                    binding.textBreeds.visibility = View.VISIBLE
                    binding.recyclerViewBreeds.visibility = View.GONE
                } else {
                    binding.textBreeds.visibility = View.GONE
                    binding.recyclerViewBreeds.visibility = View.VISIBLE
                }
            }
        }

        // Наблюдаем за поисковым запросом
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                println("DEBUG: Current search query: '$query'")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}