package com.nammapustaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammapustaka.data.model.Book
import com.nammapustaka.data.model.Review
import com.nammapustaka.data.repository.AiRepository
import com.nammapustaka.data.repository.BookRepository
import com.nammapustaka.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val aiRepo: AiRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Resource<Book>>(Resource.Loading())
    val book: StateFlow<Resource<Book>> = _book.asStateFlow()

    private val _reviews = MutableStateFlow<Resource<List<Review>>>(Resource.Loading())
    val reviews: StateFlow<Resource<List<Review>>> = _reviews.asStateFlow()

    private val _aiSummary = MutableStateFlow<Resource<String>>(Resource.Loading())
    val aiSummary: StateFlow<Resource<String>> = _aiSummary.asStateFlow()

    private val _aiSummaryKannada = MutableStateFlow<Resource<String>?>(null)
    val aiSummaryKannada: StateFlow<Resource<String>?> = _aiSummaryKannada.asStateFlow()

    private val _borrowResult = MutableStateFlow<Resource<Unit>?>(null)
    val borrowResult: StateFlow<Resource<Unit>?> = _borrowResult.asStateFlow()

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            bookRepo.getBookById(bookId).collect { result ->
                _book.value = result
                if (result is Resource.Success) {
                    loadAiSummary(result.data)
                }
            }
        }
        viewModelScope.launch {
            bookRepo.getReviews(bookId).collect { _reviews.value = it }
        }
    }

    private fun loadAiSummary(book: Book) {
        viewModelScope.launch {
            aiRepo.getBookSummary(book, inKannada = false).collect {
                _aiSummary.value = it
            }
        }
    }

    fun loadKannadaSummary(book: Book) {
        viewModelScope.launch {
            aiRepo.getBookSummary(book, inKannada = true).collect {
                _aiSummaryKannada.value = it
            }
        }
    }

    fun borrowBook(transaction: com.nammapustaka.data.model.BookTransaction) {
        viewModelScope.launch {
            _borrowResult.value = Resource.Loading()
            _borrowResult.value = bookRepo.issueBook(transaction)
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            bookRepo.addReview(review)
        }
    }
}
