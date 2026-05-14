package com.nammapustaka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammapustaka.data.model.Review
import com.nammapustaka.databinding.ItemReviewBinding
import com.nammapustaka.utils.loadAvatar
import com.nammapustaka.utils.toFormattedDate

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvReviewerName.text = review.userName
            binding.tvReviewDate.text = review.createdAt.toFormattedDate()
            binding.tvReviewText.text = review.comment
            binding.ratingBar.rating = review.rating
            binding.ivAvatar.loadAvatar(review.userAvatarUrl)
        }
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(old: Review, new: Review) = old.id == new.id
        override fun areContentsTheSame(old: Review, new: Review) = old == new
    }
}
