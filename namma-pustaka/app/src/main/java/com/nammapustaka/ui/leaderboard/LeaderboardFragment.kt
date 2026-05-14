package com.nammapustaka.ui.leaderboard

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
import com.nammapustaka.databinding.FragmentLeaderboardBinding
import com.nammapustaka.ui.adapter.LeaderboardAdapter
import com.nammapustaka.utils.Resource
import com.nammapustaka.utils.loadAvatar
import com.nammapustaka.viewmodel.LeaderboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LeaderboardViewModel by viewModels()
    private val adapter = LeaderboardAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvLeaderboard.adapter = adapter
        observeViewModels()
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.leaderboard.collect { result ->
                        binding.progressBar.isVisible = result is Resource.Loading
                        binding.rvLeaderboard.isVisible = result is Resource.Success

                        if (result is Resource.Success) {
                            adapter.submitList(result.data)
                            // Populate top 3 podium
                            val top3 = result.data.take(3)
                            top3.getOrNull(0)?.let { first ->
                                binding.tvRank1Name.text = first.userName
                                binding.tvRank1Books.text = "${first.booksRead} books"
                                binding.ivRank1Avatar.loadAvatar(first.userAvatarUrl)
                            }
                            top3.getOrNull(1)?.let { second ->
                                binding.tvRank2Name.text = second.userName
                                binding.tvRank2Books.text = "${second.booksRead} books"
                                binding.ivRank2Avatar.loadAvatar(second.userAvatarUrl)
                            }
                            top3.getOrNull(2)?.let { third ->
                                binding.tvRank3Name.text = third.userName
                                binding.tvRank3Books.text = "${third.booksRead} books"
                                binding.ivRank3Avatar.loadAvatar(third.userAvatarUrl)
                            }
                        }
                    }
                }

                launch {
                    viewModel.currentUserEntry.collect { entry ->
                        entry?.let {
                            binding.cardMyRank.isVisible = true
                            binding.tvMyRank.text = "#${it.rank}"
                            binding.tvMyName.text = it.userName
                            binding.tvMyBooks.text = "${it.booksRead} books read"
                            binding.tvMyStreak.text = "${it.streak} day streak"
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
