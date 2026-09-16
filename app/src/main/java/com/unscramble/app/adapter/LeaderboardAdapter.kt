package com.unscramble.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unscramble.app.databinding.ItemLeaderboardBinding
import com.unscramble.app.model.User

class LeaderboardAdapter(
    private val userList: List<User>
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    inner class LeaderboardViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, position: Int) {
            // position 0'dan başladığı için 1 ekleyerek sırayı buluyoruz
            binding.tvRank.text = "${position + 1}."
            binding.tvItemUsername.text = user.username
            binding.tvItemScore.text = user.score.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(userList[position], position)
    }

    override fun getItemCount(): Int = userList.size
}