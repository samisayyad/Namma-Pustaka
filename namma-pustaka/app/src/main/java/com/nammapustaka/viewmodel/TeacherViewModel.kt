package com.nammapustaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class DashboardStats(
    val totalBooks: Int = 0,
    val booksIssued: Int = 0,
    val overdueBooks: Int = 0,
    val activeStudents: Int = 0
)

@HiltViewModel
class TeacherViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    private val _recentTransactions = MutableStateFlow<Resource<List<BookTransaction>>>(Resource.Loading())
    val recentTransactions: StateFlow<Resource<List<BookTransaction>>> = _recentTransactions.asStateFlow()

    private var _schoolId = ""

    init {
        viewModelScope.launch {
            userRepo.getCurrentUser().collect { result ->
                if (result is Resource.Success) {
                    _schoolId = result.data.schoolId
                    loadDashboard()
                }
            }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            bookRepo.getBooks(_schoolId).collect { booksResult ->
                if (booksResult is Resource.Success) {
                    val totalBooks = booksResult.data.size
                    val booksIssued = booksResult.data.count { !it.isAvailable }
                    _dashboardStats.value = _dashboardStats.value.copy(
                        totalBooks = totalBooks,
                        booksIssued = booksIssued
                    )
                }
            }
        }
        viewModelScope.launch {
            bookRepo.getActiveTransactions(_schoolId).collect { result ->
                _recentTransactions.value = result
                if (result is Resource.Success) {
                    val now = System.currentTimeMillis()
                    val overdue = result.data.count { it.dueDate < now && it.returnedAt == null }
                    val activeStudents = result.data.map { it.studentId }.distinct().size
                    _dashboardStats.value = _dashboardStats.value.copy(
                        overdueBooks = overdue,
                        activeStudents = activeStudents
                    )
                }
            }
        }
    }
}
