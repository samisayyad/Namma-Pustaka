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
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _scannedBook = MutableStateFlow<Resource<Book>?>(null)
    val scannedBook: StateFlow<Resource<Book>?> = _scannedBook.asStateFlow()

    private val _transactionResult = MutableStateFlow<Resource<Unit>?>(null)
    val transactionResult: StateFlow<Resource<Unit>?> = _transactionResult.asStateFlow()

    private val _scanMode = MutableStateFlow(ScanMode.BORROW)
    val scanMode: StateFlow<ScanMode> = _scanMode.asStateFlow()

    private var _isProcessing = false
    private var _currentUser: com.nammapustaka.data.model.User? = null
    private var _schoolId: String = ""

    init {
        viewModelScope.launch {
            userRepo.getCurrentUser().collect { result ->
                if (result is Resource.Success) {
                    _currentUser = result.data
                    _schoolId = result.data.schoolId
                }
            }
        }
    }

    fun onQrCodeDetected(qrCode: String) {
        if (_isProcessing) return
        _isProcessing = true
        viewModelScope.launch {
            bookRepo.getBookByQrCode(qrCode, _schoolId).collect { result ->
                _scannedBook.value = result
                if (result !is Resource.Loading) _isProcessing = false
            }
        }
    }

    fun setScanMode(mode: ScanMode) { _scanMode.value = mode }

    fun confirmBorrow(book: Book) {
        val user = _currentUser ?: return
        val dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14)
        val transaction = BookTransaction(
            id = UUID.randomUUID().toString(),
            bookId = book.id,
            bookTitle = book.title,
            bookCoverUrl = book.coverUrl,
            studentId = user.id,
            studentName = user.name,
            issuedAt = System.currentTimeMillis(),
            dueDate = dueDate,
            schoolId = _schoolId
        )
        viewModelScope.launch {
            _transactionResult.value = Resource.Loading()
            _transactionResult.value = bookRepo.issueBook(transaction)
        }
    }

    fun confirmReturn(transactionId: String, bookId: String) {
        viewModelScope.launch {
            _transactionResult.value = Resource.Loading()
            _transactionResult.value = bookRepo.returnBook(transactionId, bookId)
        }
    }

    fun resetScan() {
        _scannedBook.value = null
        _transactionResult.value = null
        _isProcessing = false
    }

    enum class ScanMode { BORROW, RETURN }
}
