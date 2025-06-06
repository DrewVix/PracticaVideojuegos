package com.example.practicavideojuegos.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practicavideojuegos.R
import com.example.practicavideojuegos.data.db.GameDatabase
import com.example.practicavideojuegos.data.entity.Game
import com.example.practicavideojuegos.data.entity.Platform
import com.example.practicavideojuegos.data.repository.GameRepository
import com.example.practicavideojuegos.databinding.ActivityDetailGameBinding
import com.example.practicavideojuegos.ui.edit.EditGameActivity
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class GameDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailGameBinding
    private lateinit var repository: GameRepository
    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var game: Game
    private var gameId: Int = -1

    companion object {
        const val EXTRA_GAME_ID = "extra_game_id"
    }

    // Registrar el launcher para el resultado
    private val editGameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadGameDetails(gameId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Game Details"

        val database = GameDatabase.getDatabase(this)
        repository = GameRepository(database)

        setupRecyclerView()
        setupButtons()

        gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        if (gameId != -1) {
            loadGameDetails(gameId)
        } else {
            finish() // Invalid game ID
        }
    }

    private fun setupRecyclerView() {
        playerAdapter = PlayerAdapter()
        binding.recyclerViewPlayers.apply {
            adapter = playerAdapter
            layoutManager = LinearLayoutManager(this@GameDetailActivity)
        }
    }

    private fun setupButtons() {
        binding.buttonEditGame.setOnClickListener {
            val intent = Intent(this, EditGameActivity::class.java)
            intent.putExtra(EditGameActivity.EXTRA_GAME_ID, game.id)
            editGameLauncher.launch(intent)
        }

        binding.buttonDeleteGame.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadGameDetails(gameId: Int) {
        lifecycleScope.launch {
            try {
                // Load game details
                val gameDetails = repository.getGameById(gameId)
                if (gameDetails != null) {
                    game = gameDetails
                    displayGameInfo(game)
                    loadPlayers(gameId)
                    loadPlatforms(gameId)
                } else {
                    // Game not found
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }
    }

    private fun displayGameInfo(game: Game) {
        binding.textViewGameName.text = game.name
        binding.textViewGameGenre.text = game.genre
        binding.textViewGameStatus.text = game.status

        // Set status background color
        val statusColor = when (game.status.lowercase()) {
            "completed" -> ContextCompat.getColor(this, R.color.status_completed)
            "playing" -> ContextCompat.getColor(this, R.color.status_playing)
            "pending" -> ContextCompat.getColor(this, R.color.status_pending)
            else -> ContextCompat.getColor(this, R.color.status_default)
        }
        binding.textViewGameStatus.setBackgroundColor(statusColor)

        // Set game image
        binding.imageViewGameDetail.setImageURI(game.image?.toUri())
    }

    private fun loadPlayers(gameId: Int) {
        lifecycleScope.launch {
            try {
                val players = repository.getPlayersForGame(gameId)
                if (players.isNotEmpty()) {
                    playerAdapter.submitList(players)
                    binding.recyclerViewPlayers.visibility = View.VISIBLE
                    binding.textViewNoPlayers.visibility = View.GONE
                } else {
                    binding.recyclerViewPlayers.visibility = View.GONE
                    binding.textViewNoPlayers.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.recyclerViewPlayers.visibility = View.GONE
                binding.textViewNoPlayers.visibility = View.VISIBLE
            }
        }
    }

    private fun loadPlatforms(gameId: Int) {
        lifecycleScope.launch {
            try {
                val platforms = repository.getPlatformsForGame(gameId)
                if (platforms.isNotEmpty()) {
                    displayPlatforms(platforms)
                    binding.chipGroupPlatforms.visibility = View.VISIBLE
                    binding.textViewNoPlatforms.visibility = View.GONE
                } else {
                    binding.chipGroupPlatforms.visibility = View.GONE
                    binding.textViewNoPlatforms.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.chipGroupPlatforms.visibility = View.GONE
                binding.textViewNoPlatforms.visibility = View.VISIBLE
            }
        }
    }

    private fun displayPlatforms(platforms: List<Platform>) {
        binding.chipGroupPlatforms.removeAllViews()

        platforms.forEach { platform ->
            val chip = Chip(this)
            chip.text = platform.name
            chip.isClickable = false
            chip.isCheckable = false
            binding.chipGroupPlatforms.addView(chip)
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Game")
            .setMessage("Are you sure you want to delete '${game.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteGame()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteGame() {
        lifecycleScope.launch {
            try {
                repository.deleteGame(game)
                android.widget.Toast.makeText(this@GameDetailActivity, "Game deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@GameDetailActivity, "Error deleting game: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
    }
}