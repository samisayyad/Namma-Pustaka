package com.nammapustaka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammapustaka.data.model.Book
import com.nammapustaka.databinding.ItemBookCardBinding
import com.nammapustaka.utils.loadBookCover

class BookHorizontalAdapter(
    private val onBookClick: (Book) -> Unit
) : ListAdapter<Book, BookHorizontalAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(private val binding: ItemBookCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.ivBookCover.loadBookCover(book.coverUrl)
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = book.author
            binding.tvRating.text = String.format("%.1f", book.rating)
            binding.tvGenre.text = book.genre
            binding.chipAvailability.text = if (book.isAvailable) "Available" else "Borrowed"
            binding.chipAvailability.setChipBackgroundColorResource(
                if (book.isAvailable) com.nammapustaka.R.color.success_green_light
                else com.nammapustaka.R.color.error_red_light
            )
            binding.root.setOnClickListener { onBookClick(book) }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
    }
}
