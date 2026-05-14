package com.nammapustaka.ui.teacher

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
import com.nammapustaka.databinding.FragmentTeacherDashboardBinding
import com.nammapustaka.utils.Resource
import com.nammapustaka.viewmodel.TeacherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TeacherDashboardFragment : Fragment() {

    private var _binding: FragmentTeacherDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TeacherViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModels()
    }

    private fun setupClickListeners() {
        binding.cardAddBook.setOnClickListener {
            findNavController().navigate(com.nammapustaka.R.id.action_teacher_to_add_book)
        }
        binding.cardViewAnalytics.setOnClickListener {
            findNavController().navigate(com.nammapustaka.R.id.action_teacher_to_analytics)
        }
        binding.cardManageStudents.setOnClickListener {
            findNavController().navigate(com.nammapustaka.R.id.action_teacher_to_students)
        }
        binding.cardOverdueBooks.setOnClickListener {
            findNavController().navigate(com.nammapustaka.R.id.action_teacher_to_overdue)
        }
        binding.ivLogout.setOnClickListener {
            findNavController().navigate(com.nammapustaka.R.id.action_teacher_to_login)
        }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dashboardStats.collect { stats ->
                        binding.tvTotalBooks.text = stats.totalBooks.toString()
                        binding.tvBooksIssued.text = stats.booksIssued.toString()
                        binding.tvOverdueBooks.text = stats.overdueBooks.toString()
                        binding.tvActiveStudents.text = stats.activeStudents.toString()
                    }
                }

                launch {
                    viewModel.recentTransactions.collect { result ->
                        binding.progressBar.isVisible = result is Resource.Loading
                        if (result is Resource.Success) {
                            binding.tvRecentActivity.text = buildString {
                                result.data.take(5).forEach { tx ->
                                    appendLine("• ${tx.studentName} borrowed '${tx.bookTitle}'")
                                }
                            }
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
