package com.nammapustaka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammapustaka.data.model.Book
import com.nammapustaka.databinding.ItemBookGridBinding
import com.nammapustaka.utils.loadBookCover

class BookGridAdapter(
    private val onBookClick: (Book) -> Unit
) : ListAdapter<Book, BookGridAdapter.BookGridViewHolder>(BookGridDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookGridViewHolder {
        val binding = ItemBookGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookGridViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookGridViewHolder(private val binding: ItemBookGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.ivBookCover.loadBookCover(book.coverUrl)
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = book.author
            binding.tvRating.text = String.format("%.1f", book.rating)
            val available = book.isAvailable
            binding.ivAvailabilityDot.setImageResource(
                if (available) com.nammapustaka.R.drawable.bg_available_badge
                else com.nammapustaka.R.drawable.bg_borrowed_badge
            )
            binding.root.setOnClickListener { onBookClick(book) }
        }
    }

    class BookGridDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
    }
}
