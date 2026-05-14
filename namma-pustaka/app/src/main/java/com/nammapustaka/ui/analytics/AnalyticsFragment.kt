package com.nammapustaka.ui.analytics

import android.graphics.Color
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
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.nammapustaka.databinding.FragmentAnalyticsBinding
import com.nammapustaka.utils.Resource
import com.nammapustaka.viewmodel.AnalyticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        setupPieChart()
        observeAnalytics()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 50f
            transparentCircleRadius = 55f
            setHoleColor(Color.TRANSPARENT)
            legend.isEnabled = true
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(10f)
        }
    }

    private fun observeAnalytics() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.analytics.collect { result ->
                        binding.progressBar.isVisible = result is Resource.Loading
                        binding.contentGroup.isVisible = result is Resource.Success

                        if (result is Resource.Success) {
                            val data = result.data
                            binding.tvTotalBooks.text = data.totalBooks.toString()
                            binding.tvOverdueBooks.text = data.overdueCount.toString()
                            binding.tvAvgRating.text = String.format("%.1f", data.averageRating)

                            // Populate top books
                            binding.tvTopBooks.text = data.topBooks.mapIndexed { i, book ->
                                "${i + 1}. ${book.title} — ${book.borrowCount} borrows"
                            }.joinToString("\n")

                            // Pie chart
                            if (data.genreBreakdown.isNotEmpty()) {
                                val entries = data.genreBreakdown.map { (genre, count) ->
                                    PieEntry(count.toFloat(), genre)
                                }
                                val colors = listOf(
                                    Color.parseColor("#6750A4"),
                                    Color.parseColor("#7B61FF"),
                                    Color.parseColor("#00897B"),
                                    Color.parseColor("#FF6B35"),
                                    Color.parseColor("#3F88C5"),
                                    Color.parseColor("#F7B731"),
                                    Color.parseColor("#E91E63"),
                                    Color.parseColor("#4CAF50")
                                )
                                val dataSet = PieDataSet(entries, "Genres").apply {
                                    this.colors = colors.take(entries.size)
                                    sliceSpace = 2f
                                    valueTextSize = 10f
                                    valueTextColor = Color.WHITE
                                }
                                binding.pieChart.data = PieData(dataSet)
                                binding.pieChart.invalidate()
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
