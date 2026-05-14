package com.nammapustaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammapustaka.data.model.Book
import com.nammapustaka.data.model.User
import com.nammapustaka.data.repository.BookRepository
import com.nammapustaka.data.repository.UserRepository
import com.nammapustaka.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<Resource<User>>(Resource.Loading())
    val currentUser: StateFlow<Resource<User>> = _currentUser.asStateFlow()

    private val _recommendedBooks = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val recommendedBooks: StateFlow<Resource<List<Book>>> = _recommendedBooks.asStateFlow()

    private val _trendingBooks = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val trendingBooks: StateFlow<Resource<List<Book>>> = _trendingBooks.asStateFlow()

    private val _newArrivals = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val newArrivals: StateFlow<Resource<List<Book>>> = _newArrivals.asStateFlow()

    private val _continueReading = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val continueReading: StateFlow<Resource<List<Book>>> = _continueReading.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            userRepo.getCurrentUser().collect { result ->
                _currentUser.value = result
                if (result is Resource.Success) {
                    val schoolId = result.data.schoolId
                    loadBooks(schoolId)
                }
            }
        }
    }

    private fun loadBooks(schoolId: String) {
        viewModelScope.launch {
            bookRepo.getBooks(schoolId).collect { books ->
                when (books) {
                    is Resource.Success -> {
                        val all = books.data
                        _recommendedBooks.value = Resource.Success(
                            all.filter { it.rating >= 4.0f }.shuffled().take(10)
                        )
                        _trendingBooks.value = Resource.Success(
                            all.sortedByDescending { it.borrowCount }.take(10)
                        )
                        _newArrivals.value = Resource.Success(
                            all.sortedByDescending { it.addedAt }.take(8)
                        )
                    }
                    is Resource.Error -> {
                        _recommendedBooks.value = Resource.Error(books.message)
                    }
                    is Resource.Loading -> {}
                }
            }
        }

        viewModelScope.launch {
            val uid = userRepo.currentUserId ?: return@launch
            bookRepo.getStudentTransactions(uid).collect { result ->
                if (result is Resource.Success) {
                    val activeTransactionBookIds = result.data
                        .filter { it.returnedAt == null }
                        .map { it.bookId }
                    if (activeTransactionBookIds.isNotEmpty()) {
                        bookRepo.getBooks(schoolId).collect { books ->
                            if (books is Resource.Success) {
                                val activeBooks = books.data.filter { it.id in activeTransactionBookIds }
                                _continueReading.value = Resource.Success(activeBooks)
                            }
                        }
                    } else {
                        _continueReading.value = Resource.Success(emptyList())
                    }
                }
            }
        }
    }
}
