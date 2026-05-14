package com.nammapustaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammapustaka.data.model.Book
import com.nammapustaka.data.repository.BookRepository
import com.nammapustaka.data.repository.UserRepository
import com.nammapustaka.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _books = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val books: StateFlow<Resource<List<Book>>> = _books.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow("All")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _schoolId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            userRepo.getCurrentUser().collect { result ->
                if (result is Resource.Success) {
                    _schoolId.value = result.data.schoolId
                    loadBooks()
                }
            }
        }

        // Debounced search
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        bookRepo.searchBooks(_schoolId.value, query).collect {
                            _books.value = it
                        }
                    } else {
                        loadBooks()
                    }
                }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            val genre = if (_selectedGenre.value == "All") null else _selectedGenre.value
            bookRepo.getBooks(_schoolId.value, genre).collect { _books.value = it }
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun setGenre(genre: String) {
        _selectedGenre.value = genre
        loadBooks()
    }
}
