package com.nammapustaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammapustaka.data.model.Book
import com.nammapustaka.data.model.BookTransaction
import com.nammapustaka.data.repository.BookRepository
import com.nammapustaka.data.repository.UserRepository
import com.nammapustaka.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsData(
    val totalBooks: Int = 0,
    val genreBreakdown: Map<String, Int> = emptyMap(),
    val monthlyBorrows: List<Pair<String, Int>> = emptyList(),
    val topBooks: List<Book> = emptyList(),
    val overdueCount: Int = 0,
    val averageRating: Float = 0f
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _analytics = MutableStateFlow<Resource<AnalyticsData>>(Resource.Loading())
    val analytics: StateFlow<Resource<AnalyticsData>> = _analytics.asStateFlow()

    private val _transactions = MutableStateFlow<Resource<List<BookTransaction>>>(Resource.Loading())
    val transactions: StateFlow<Resource<List<BookTransaction>>> = _transactions.asStateFlow()

    init {
        viewModelScope.launch {
            userRepo.getCurrentUser().collect { result ->
                if (result is Resource.Success) {
                    val schoolId = result.data.schoolId
                    loadAnalytics(schoolId)
                }
            }
        }
    }

    private fun loadAnalytics(schoolId: String) {
        viewModelScope.launch {
            bookRepo.getBooks(schoolId).collect { booksResult ->
                if (booksResult is Resource.Success) {
                    val books = booksResult.data
                    val genreMap = books.groupBy { it.genre }.mapValues { it.value.size }
                    val topBooks = books.sortedByDescending { it.borrowCount }.take(5)
                    val avgRating = if (books.isNotEmpty()) books.map { it.rating }.average().toFloat() else 0f

                    _analytics.value = Resource.Success(
                        AnalyticsData(
                            totalBooks = books.size,
                            genreBreakdown = genreMap,
                            topBooks = topBooks,
                            averageRating = avgRating
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            bookRepo.getActiveTransactions(schoolId).collect { result ->
                _transactions.value = result
                val currentAnalytics = (_analytics.value as? Resource.Success)?.data ?: return@collect
                if (result is Resource.Success) {
                    val now = System.currentTimeMillis()
                    val overdue = result.data.count { it.dueDate < now && it.returnedAt == null }
                    _analytics.value = Resource.Success(currentAnalytics.copy(overdueCount = overdue))
                }
            }
        }
    }
}
