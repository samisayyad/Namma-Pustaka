package com.nammapustaka.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.nammapustaka.R
import com.nammapustaka.databinding.FragmentBookCatalogBinding
import com.nammapustaka.ui.adapter.BookGridAdapter
import com.nammapustaka.utils.Resource
import com.nammapustaka.viewmodel.CatalogViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CatalogFragment : Fragment() {

    private var _binding: FragmentBookCatalogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CatalogViewModel by viewModels()
    private var isGridMode = true

    private val gridAdapter = BookGridAdapter { book ->
        findNavController().navigate(
            CatalogFragmentDirections.actionCatalogToBookDetail(book.id)
        )
    }

    private val genres = listOf(
        "All", "Fiction", "Non-Fiction", "Science", "History",
        "Biography", "Mathematics", "Language", "Arts", "Sports"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGenreChips()
        setupSearch()
        setupRecyclerView()
        setupToggle()
        observeBooks()
    }

    private fun setupGenreChips() {
        genres.forEach { genre ->
            val chip = Chip(requireContext()).apply {
                text = genre
                isCheckable = true
                isChecked = genre == "All"
                setChipBackgroundColorResource(R.color.chip_catalog_selector)
                setTextColor(resources.getColorStateList(R.color.chip_text_color, null))
                setOnClickListener { viewModel.setGenre(genre) }
            }
            binding.chipGroupGenre.addView(chip)
        }
    }

    private fun setupSearch() {
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }
    }

    private fun setupRecyclerView() {
        binding.rvBooks.adapter = gridAdapter
    }

    private fun setupToggle() {
        binding.btnToggleView.setOnClickListener {
            isGridMode = !isGridMode
            val spanCount = if (isGridMode) 2 else 1
            (binding.rvBooks.layoutManager as? androidx.recyclerview.widget.GridLayoutManager)
                ?.spanCount = spanCount
            binding.btnToggleView.setImageResource(
                if (isGridMode) R.drawable.ic_grid else R.drawable.ic_list
            )
        }
    }

    private fun observeBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.books.collect { result ->
                    binding.progressBar.isVisible = result is Resource.Loading
                    binding.rvBooks.isVisible = result is Resource.Success
                    binding.tvEmpty.isVisible = result is Resource.Success && (result as Resource.Success).data.isEmpty()

                    if (result is Resource.Success) {
                        gridAdapter.submitList(result.data)
                        binding.tvBooksCount.text = "${result.data.size} books found"
                    } else if (result is Resource.Error) {
                        binding.tvEmpty.isVisible = true
                        binding.tvEmpty.text = result.message
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
