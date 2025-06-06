package com.example.practicavideojuegos.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.practicavideojuegos.R
import com.example.practicavideojuegos.data.entity.Game
import com.example.practicavideojuegos.databinding.ItemGameBinding

class GameAdapter(private val onGameClick: (Game) -> Unit) :
    ListAdapter<Game, GameAdapter.GameViewHolder>(GameDiffCallback()) {

    class GameViewHolder(private val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(game: Game, onGameClick: (Game) -> Unit) {
            binding.textViewGameName.text = game.name
            binding.textViewGameGenre.text = game.genre
            binding.textViewGameStatus.text = game.status

            // Set status background color based on status
            val statusColor = when (game.status.lowercase()) {
                "completed" -> ContextCompat.getColor(binding.root.context, R.color.status_completed)
                "playing" -> ContextCompat.getColor(binding.root.context, R.color.status_playing)
                "pending" -> ContextCompat.getColor(binding.root.context, R.color.status_pending)
                else -> ContextCompat.getColor(binding.root.context, R.color.status_default)
            }
            binding.textViewGameStatus.setBackgroundColor(statusColor)

            binding.imageViewGame.setImageURI(game.image?.toUri())

            binding.root.setOnClickListener {
                onGameClick(game)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position), onGameClick)
    }
}

class GameDiffCallback : DiffUtil.ItemCallback<Game>() {
    override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
        return oldItem == newItem
    }
}