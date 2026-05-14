package com.nammapustaka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammapustaka.R
import com.nammapustaka.data.model.LeaderboardEntry
import com.nammapustaka.databinding.ItemLeaderboardBinding
import com.nammapustaka.utils.loadAvatar

class LeaderboardAdapter : ListAdapter<LeaderboardEntry, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LeaderboardViewHolder(private val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LeaderboardEntry) {
            binding.tvRank.text = "#${entry.rank}"
            binding.tvName.text = entry.userName
            binding.tvClass.text = "Class ${entry.classGrade}"
            binding.tvBooksRead.text = "${entry.booksRead} books"
            binding.tvStreak.text = "${entry.streak}🔥"
            binding.ivAvatar.loadAvatar(entry.userAvatarUrl)

            val bgColor = when {
                entry.isCurrentUser -> R.color.primary_light
                entry.rank == 1 -> R.color.gold_light
                entry.rank == 2 -> R.color.silver_light
                entry.rank == 3 -> R.color.bronze_light
                else -> R.color.surface_light
            }
            binding.root.setBackgroundResource(bgColor)

            binding.tvRank.setTextColor(
                binding.root.context.getColor(
                    when (entry.rank) {
                        1 -> R.color.gold
                        2 -> R.color.silver
                        3 -> R.color.bronze
                        else -> R.color.on_surface
                    }
                )
            )

            if (entry.rank <= 3) {
                binding.ivCrown.visibility = android.view.View.VISIBLE
                binding.ivCrown.setImageResource(
                    when (entry.rank) {
                        1 -> R.drawable.ic_crown_gold
                        2 -> R.drawable.ic_crown_silver
                        else -> R.drawable.ic_crown_bronze
                    }
                )
            } else {
                binding.ivCrown.visibility = android.view.View.GONE
            }
        }
    }

    class LeaderboardDiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(old: LeaderboardEntry, new: LeaderboardEntry) = old.userId == new.userId
        override fun areContentsTheSame(old: LeaderboardEntry, new: LeaderboardEntry) = old == new
    }
}
