package com.nammapustaka.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nammapustaka.data.model.Book
import com.nammapustaka.data.repository.BookRepository
import com.nammapustaka.data.repository.UserRepository
import com.nammapustaka.databinding.FragmentLoginBinding
import com.nammapustaka.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class AddBookDialogFragment : BottomSheetDialogFragment() {

    @Inject lateinit var bookRepo: BookRepository
    @Inject lateinit var userRepo: UserRepository

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.text = "Add Book"
        binding.btnLogin.setOnClickListener {
            val title = binding.etEmail.text?.toString()?.trim() ?: ""
            if (title.isEmpty()) {
                showToast("Please enter book title")
                return@setOnClickListener
            }
            val book = Book(
                id = UUID.randomUUID().toString(),
                title = title,
                author = "Unknown",
                genre = "General",
                qrCode = UUID.randomUUID().toString(),
                addedAt = System.currentTimeMillis(),
                isAvailable = true
            )
            CoroutineScope(Dispatchers.Main).launch {
                bookRepo.addBook(book)
                showToast("Book added successfully!")
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
