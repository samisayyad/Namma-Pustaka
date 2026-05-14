package com.nammapustaka.ui.bookdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nammapustaka.data.model.BookTransaction
import com.nammapustaka.databinding.FragmentBookDetailBinding
import com.nammapustaka.ui.adapter.ReviewAdapter
import com.nammapustaka.utils.Resource
import com.nammapustaka.utils.loadBookCover
import com.nammapustaka.utils.showToast
import com.nammapustaka.utils.toFormattedDate
import com.nammapustaka.viewmodel.BookDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookDetailViewModel by viewModels()
    private val args: BookDetailFragmentArgs by navArgs()
    private val reviewAdapter = ReviewAdapter()
    private var currentBook: com.nammapustaka.data.model.Book? = null
    private var summaryInKannada = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadBook(args.bookId)
        setupRecyclerViews()
        setupClickListeners()
        observeViewModels()
    }

    private fun setupRecyclerViews() {
        binding.rvReviews.adapter = reviewAdapter
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnBorrow.setOnClickListener {
            val book = currentBook ?: return@setOnClickListener
            if (!book.isAvailable) {
                showToast("This book is currently unavailable")
                return@setOnClickListener
            }
            showBorrowDialog(book)
        }

        binding.toggleSummaryLang.setOnCheckedChangeListener { _, isChecked ->
            summaryInKannada = isChecked
            if (isChecked && currentBook != null) {
                viewModel.loadKannadaSummary(currentBook!!)
            }
        }

        binding.ibShare.setOnClickListener {
            val book = currentBook ?: return@setOnClickListener
            val shareText = "Check out '${book.title}' by ${book.author} on Namma Pustaka!"
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }
            startActivity(android.content.Intent.createChooser(intent, "Share Book"))
        }
    }

    private fun showBorrowDialog(book: com.nammapustaka.data.model.Book) {
        val dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Borrow '${book.title}'?")
            .setMessage("Due date: ${dueDate.toFormattedDate()}\n\nPlease return within 14 days.")
            .setPositiveButton("Borrow") { _, _ ->
                val transaction = BookTransaction(
                    id = UUID.randomUUID().toString(),
                    bookId = book.id,
                    bookTitle = book.title,
                    bookCoverUrl = book.coverUrl,
                    studentId = "",
                    studentName = "",
                    issuedAt = System.currentTimeMillis(),
                    dueDate = dueDate,
                    schoolId = book.schoolId
                )
                viewModel.borrowBook(transaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.book.collect { result ->
                        binding.progressBar.isVisible = result is Resource.Loading
                        binding.contentGroup.isVisible = result is Resource.Success
                        if (result is Resource.Success) {
                            val book = result.data
                            currentBook = book
                            binding.ivBookCover.loadBookCover(book.coverUrl)
                            binding.tvBookTitle.text = book.title
                            binding.tvAuthor.text = book.author
                            binding.tvGenre.text = book.genre
                            binding.tvRating.text = String.format("%.1f", book.rating)
                            binding.ratingBar.rating = book.rating
                            binding.tvDescription.text = book.description
                            binding.tvAvailability.text = if (book.isAvailable) "Available" else "Borrowed"
                            binding.tvAvailability.setTextColor(
                                resources.getColor(
                                    if (book.isAvailable) com.nammapustaka.R.color.success_green
                                    else com.nammapustaka.R.color.error_red,
                                    null
                                )
                            )
                            binding.btnBorrow.isEnabled = book.isAvailable
                        }
                    }
                }

                launch {
                    viewModel.reviews.collect { result ->
                        if (result is Resource.Success) {
                            reviewAdapter.submitList(result.data)
                            binding.tvReviewCount.text = "${result.data.size} reviews"
                        }
                    }
                }

                launch {
                    viewModel.aiSummary.collect { result ->
                        when (result) {
                            is Resource.Loading -> {
                                binding.tvAiSummary.text = "Generating AI summary..."
                                binding.progressAiSummary.isVisible = true
                            }
                            is Resource.Success -> {
                                binding.progressAiSummary.isVisible = false
                                if (!summaryInKannada) binding.tvAiSummary.text = result.data
                            }
                            is Resource.Error -> {
                                binding.progressAiSummary.isVisible = false
                                binding.tvAiSummary.text = "Summary unavailable"
                            }
                        }
                    }
                }

                launch {
                    viewModel.aiSummaryKannada.collect { result ->
                        if (summaryInKannada && result != null) {
                            when (result) {
                                is Resource.Success -> binding.tvAiSummary.text = result.data
                                is Resource.Loading -> binding.tvAiSummary.text = "ಸಾರಾಂಶ ತಯಾರಿಸಲಾಗುತ್ತಿದೆ..."
                                is Resource.Error -> binding.tvAiSummary.text = "ಸಾರಾಂಶ ಲಭ್ಯವಿಲ್ಲ"
                            }
                        }
                    }
                }

                launch {
                    viewModel.borrowResult.collect { result ->
                        when (result) {
                            is Resource.Success -> showToast("Book borrowed successfully!")
                            is Resource.Error -> showToast(result.message)
                            else -> {}
                        }
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
