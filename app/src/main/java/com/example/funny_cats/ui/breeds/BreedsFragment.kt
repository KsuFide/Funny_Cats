package com.example.funny_cats.ui.breeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

    // Флаг для отслеживания, нужно ли сбросить состояние
    private var shouldResetState = true

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

        // Сбрасываем состояние при каждом создании View
        resetFragmentState()
    }

    override fun onResume() {
        super.onResume()

        // Сбрасываем состояние при возврате на фрагмент
        if (shouldResetState) {
            resetFragmentState()
        }
    }

    override fun onPause() {
        super.onPause()
        // Устанавливаем флаг, что при следующем возврате нужно сбросить состояние
        shouldResetState = true
    }

    private fun resetFragmentState() {
        // Сбрасываем поиск
        binding.searchView.setQuery("", false)

        // Сбрасываем скролл
        binding.recyclerViewBreeds.scrollToPosition(0)

        // Обновляем данные
        viewModel.loadBreeds()

        // Сбрасываем флаг
        shouldResetState = false
    }

    private fun setupRecyclerView() {
        adapter = BreedsAdapter { breed ->
            // При переходе в детали устанавливаем флаг, что не нужно сбрасывать состояние
            shouldResetState = false

            val bundle = Bundle().apply {
                putString("breedId", breed.id)
            }
            findNavController().navigate(R.id.breedDetailFragment, bundle)
        }

        binding.recyclerViewBreeds.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewBreeds.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchBreeds(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.breedsPaging.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}