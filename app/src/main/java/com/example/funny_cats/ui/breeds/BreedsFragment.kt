package com.example.funny_cats.ui.breeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.funny_cats.R
import com.example.funny_cats.databinding.FragmentBreedsBinding
import dagger.hilt.android.AndroidEntryPoint
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
    }

    private fun setupRecyclerView() {
        adapter = BreedsAdapter { breed ->
            // TODO: Переход к деталям породы
        }
        binding.recyclerViewBreeds.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.recyclerViewBreeds.adapter = adapter
    }

    private fun setupSearchView() {
        // Устанавливаем подсказку
        binding.searchView.queryHint = getString(R.string.search_hint)

        // Устанавливаем слушатель
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Не нужно обрабатывать отдельно, так как мы используем текстовые изменения
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchBreeds(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredBreeds.collect { breeds ->
                adapter.submitList(breeds)
                if (breeds.isEmpty() && viewModel.searchQuery.value.isNotEmpty()) {
                    binding.textBreeds.text = "😿 Не найдено пород по вашему запросу"
                    binding.textBreeds.visibility = View.VISIBLE
                    binding.recyclerViewBreeds.visibility = View.GONE
                } else if (breeds.isEmpty()) {
                    binding.textBreeds.text = getString(R.string.loading_breeds)
                    binding.textBreeds.visibility = View.VISIBLE
                    binding.recyclerViewBreeds.visibility = View.GONE
                } else {
                    binding.textBreeds.visibility = View.GONE
                    binding.recyclerViewBreeds.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    binding.textBreeds.text = getString(R.string.loading_breeds)
                    binding.textBreeds.visibility = View.VISIBLE
                    binding.recyclerViewBreeds.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}