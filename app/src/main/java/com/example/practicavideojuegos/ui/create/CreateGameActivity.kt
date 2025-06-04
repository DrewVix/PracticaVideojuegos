package com.example.practicavideojuegos.ui.create

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practicavideojuegos.R
import com.example.practicavideojuegos.data.entity.Game
import com.example.practicavideojuegos.data.entity.Player
import com.example.practicavideojuegos.data.entity.Platform
import com.example.practicavideojuegos.data.db.GameDatabase
import com.example.practicavideojuegos.data.repository.GameRepository
import com.example.practicavideojuegos.databinding.ActivityCreateGameBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class CreateGameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGameBinding
    private lateinit var repository: GameRepository
    private var selectedImageResource = 0

    // Lists to store all available players and platforms
    private var allPlayers = mutableListOf<Player>()
    private var allPlatforms = mutableListOf<Platform>()

    // Lists to store selected players and platforms
    private var selectedPlayers = mutableListOf<Player>()
    private var selectedPlatforms = mutableListOf<Platform>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Game"

        val database = GameDatabase.getDatabase(this)
        repository = GameRepository(database)

        setupStatusDropdown()
        setupImageSelection()
        setupSaveButton()

        // Load players and platforms, then setup dropdowns
        loadPlayersAndPlatforms()
    }

    private fun setupStatusDropdown() {
        val statusOptions = arrayOf("Pending", "Playing", "Completed", "Paused")
        val adapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, statusOptions)
        binding.autoCompleteGameStatus.setAdapter(adapter)
    }

    private fun setupImageSelection() {
        binding.buttonSelectImage.setOnClickListener {
            val imageResources = arrayOf(
                R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background,
                R.drawable.ic_launcher_background
            )

            selectedImageResource = imageResources.random()
            binding.imageViewGamePreview.setImageResource(selectedImageResource)
        }
    }

    private fun loadPlayersAndPlatforms() {
        lifecycleScope.launch {
            // Load all players
            allPlayers = repository.getAllPlayers().toMutableList()
            setupPlayersDropdown()

            // Load all platforms
            allPlatforms = repository.getAllPlatforms().toMutableList()
            setupPlatformsDropdown()
        }
    }

    private fun setupPlayersDropdown() {
        if (allPlayers.isEmpty()) {
            binding.autoCompleteGamePlayers.isEnabled = false
            binding.autoCompleteGamePlayers.hint = "No players available. Create players first."
            return
        }

        val playerNames = allPlayers.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, playerNames)
        binding.autoCompleteGamePlayers.setAdapter(adapter)

        binding.autoCompleteGamePlayers.setOnItemClickListener { _, _, position, _ ->
            val selectedPlayer = allPlayers[position]

            // Check if player is already selected
            if (!selectedPlayers.any { it.id == selectedPlayer.id }) {
                selectedPlayers.add(selectedPlayer)
                addPlayerChip(selectedPlayer)
                binding.autoCompleteGamePlayers.text.clear()
            } else {
                Toast.makeText(this, "Player already selected", Toast.LENGTH_SHORT).show()
                binding.autoCompleteGamePlayers.text.clear()
            }
        }
    }

    private fun setupPlatformsDropdown() {
        if (allPlatforms.isEmpty()) {
            binding.autoCompleteGamePlatforms.isEnabled = false
            binding.autoCompleteGamePlatforms.hint = "No platforms available. Create platforms first."
            return
        }

        val platformNames = allPlatforms.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, platformNames)
        binding.autoCompleteGamePlatforms.setAdapter(adapter)

        binding.autoCompleteGamePlatforms.setOnItemClickListener { _, _, position, _ ->
            val selectedPlatform = allPlatforms[position]

            // Check if platform is already selected
            if (!selectedPlatforms.any { it.id == selectedPlatform.id }) {
                selectedPlatforms.add(selectedPlatform)
                addPlatformChip(selectedPlatform)
                binding.autoCompleteGamePlatforms.text.clear()
            } else {
                Toast.makeText(this, "Platform already selected", Toast.LENGTH_SHORT).show()
                binding.autoCompleteGamePlatforms.text.clear()
            }
        }
    }

    private fun addPlayerChip(player: Player) {
        val chip = Chip(this)
        chip.text = player.name
        chip.isCloseIconVisible = true

        chip.setOnCloseIconClickListener {
            // Remove from selected players list
            selectedPlayers.removeAll { it.id == player.id }
            // Remove chip from view
            binding.chipGroupSelectedPlayers.removeView(chip)
        }

        binding.chipGroupSelectedPlayers.addView(chip)
    }

    private fun addPlatformChip(platform: Platform) {
        val chip = Chip(this)
        chip.text = platform.name
        chip.isCloseIconVisible = true

        chip.setOnCloseIconClickListener {
            // Remove from selected platforms list
            selectedPlatforms.removeAll { it.id == platform.id }
            // Remove chip from view
            binding.chipGroupSelectedPlatforms.removeView(chip)
        }

        binding.chipGroupSelectedPlatforms.addView(chip)
    }

    private fun setupSaveButton() {
        binding.buttonSaveGame.setOnClickListener {
            saveGame()
        }
    }

    private fun saveGame() {
        val name = binding.editTextGameName.text.toString().trim()
        val genre = binding.editTextGameGenre.text.toString().trim()
        val status = binding.autoCompleteGameStatus.text.toString().trim()

        if (name.isEmpty()) {
            binding.textInputLayoutGameName.error = "Game name is required"
            return
        }

        if (genre.isEmpty()) {
            binding.textInputLayoutGameGenre.error = "Genre is required"
            return
        }

        if (status.isEmpty()) {
            binding.textInputLayoutGameStatus.error = "Status is required"
            return
        }

        val gameId = System.currentTimeMillis().toInt() // Simple ID generation
        val game = Game(
            id = gameId,
            name = name,
            genre = genre,
            status = status,
            image = if (selectedImageResource != 0) selectedImageResource else R.drawable.ic_launcher_background
        )

        lifecycleScope.launch {
            try {
                // Insert the game first
                repository.insertGame(game)

                // Assign selected players to the game
                selectedPlayers.forEach { player ->
                    repository.assignGameToPlayer(gameId, player.id)
                }

                // Assign selected platforms to the game
                selectedPlatforms.forEach { platform ->
                    repository.assignGameToPlatform(gameId, platform.id)
                }

                Toast.makeText(
                    this@CreateGameActivity,
                    "Game saved with ${selectedPlayers.size} players and ${selectedPlatforms.size} platforms",
                    Toast.LENGTH_LONG
                ).show()

                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@CreateGameActivity,
                    "Error saving game: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
}