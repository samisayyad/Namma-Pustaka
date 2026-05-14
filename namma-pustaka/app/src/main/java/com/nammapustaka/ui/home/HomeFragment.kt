package com.nammapustaka.ui.home

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
import com.nammapustaka.R
import com.nammapustaka.databinding.FragmentHomeBinding
import com.nammapustaka.ui.adapter.BookHorizontalAdapter
import com.nammapustaka.utils.Resource
import com.nammapustaka.utils.hide
import com.nammapustaka.utils.loadAvatar
import com.nammapustaka.utils.show
import com.nammapustaka.utils.toGreeting
import com.nammapustaka.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private val recommendedAdapter = BookHorizontalAdapter { book ->
        val action = HomeFragmentDirections.actionHomeToBookDetail(book.id)
        findNavController().navigate(action)
    }
    private val trendingAdapter = BookHorizontalAdapter { book ->
        val action = HomeFragmentDirections.actionHomeToBookDetail(book.id)
        findNavController().navigate(action)
    }
    private val newArrivalsAdapter = BookHorizontalAdapter { book ->
        val action = HomeFragmentDirections.actionHomeToBookDetail(book.id)
        findNavController().navigate(action)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupClickListeners()
        observeViewModels()
    }

    private fun setupRecyclerViews() {
        binding.rvRecommended.adapter = recommendedAdapter
        binding.rvTrending.adapter = trendingAdapter
        binding.rvNewArrivals.adapter = newArrivalsAdapter
    }

    private fun setupClickListeners() {
        binding.cardAiChat.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_ai_chat)
        }
        binding.btnSeeAllRecommended.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_catalog)
        }
        binding.btnSeeAllTrending.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_catalog)
        }
        binding.cardQrScan.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_qr_scanner)
        }
        binding.ivNotifications.setOnClickListener {
            // TODO: Notifications screen
        }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentUser.collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                val user = result.data
                                binding.tvGreeting.text = "".toGreeting() + ","
                                binding.tvUserName.text = user.name
                                binding.tvStreak.text = "${user.currentStreak} day streak"
                                binding.tvBooksRead.text = "${user.totalBooksRead} books"
                                binding.ivAvatar.loadAvatar(user.avatarUrl)
                            }
                            is Resource.Error -> {}
                            is Resource.Loading -> {}
                        }
                    }
                }

                launch {
                    viewModel.recommendedBooks.collect { result ->
                        binding.shimmerRecommended.isVisible = result is Resource.Loading
                        binding.rvRecommended.isVisible = result is Resource.Success
                        if (result is Resource.Success) {
                            recommendedAdapter.submitList(result.data)
                        }
                    }
                }

                launch {
                    viewModel.trendingBooks.collect { result ->
                        if (result is Resource.Success) {
                            trendingAdapter.submitList(result.data)
                        }
                    }
                }

                launch {
                    viewModel.newArrivals.collect { result ->
                        if (result is Resource.Success) {
                            newArrivalsAdapter.submitList(result.data)
                        }
                    }
                }

                launch {
                    viewModel.continueReading.collect { result ->
                        if (result is Resource.Success) {
                            val hasContinue = result.data.isNotEmpty()
                            binding.groupContinueReading.isVisible = hasContinue
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
