package com.example.practicavideojuegos.ui.edit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.practicavideojuegos.R
import com.example.practicavideojuegos.data.entity.Game
import com.example.practicavideojuegos.data.entity.Player
import com.example.practicavideojuegos.data.entity.Platform
import com.example.practicavideojuegos.data.db.GameDatabase
import com.example.practicavideojuegos.data.repository.GameRepository
import com.example.practicavideojuegos.databinding.ActivityEditGameBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class EditGameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditGameBinding
    private lateinit var repository: GameRepository
    private lateinit var currentGame: Game
    private var selectedImageUri: String? = null

    // Lists to store all available players and platforms
    private var allPlayers = mutableListOf<Player>()
    private var allPlatforms = mutableListOf<Platform>()

    // Lists to store selected players and platforms
    private var selectedPlayers = mutableListOf<Player>()
    private var selectedPlatforms = mutableListOf<Platform>()

    companion object {
        const val EXTRA_GAME_ID = "extra_game_id"
    }

    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            selectedImageUri = it.toString()
            binding.imageViewGamePreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Game"

        val database = GameDatabase.getDatabase(this)
        repository = GameRepository(database)

        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        if (gameId != -1) {
            loadGameData(gameId)
        } else {
            Toast.makeText(this, "Invalid game ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupImageSelection()
        setupSaveButton()
        setupCancelButton()
    }

    private fun loadGameData(gameId: Int) {
        lifecycleScope.launch {
            try {
                // Load game details
                val game = repository.getGameById(gameId)
                if (game != null) {
                    currentGame = game
                    displayGameInfo(game)

                    // Load all players and platforms
                    allPlayers = repository.getAllPlayers().toMutableList()
                    allPlatforms = repository.getAllPlatforms().toMutableList()

                    // Load selected players and platforms for this game
                    selectedPlayers = repository.getPlayersForGame(gameId).toMutableList()
                    selectedPlatforms = repository.getPlatformsForGame(gameId).toMutableList()
                    // Setup dropdowns and display selected items
                    setupStatusDropdown()
                    setupPlayersDropdown()
                    setupPlatformsDropdown()

                    // Display already selected players and platforms as chips
                    selectedPlayers.forEach { addPlayerChip(it) }
                    selectedPlatforms.forEach { addPlatformChip(it) }
                } else {
                    Toast.makeText(this@EditGameActivity, "Game not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditGameActivity, "Error loading game: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun displayGameInfo(game: Game) {
        binding.editTextGameName.setText(game.name)
        binding.editTextGameGenre.setText(game.genre)
        binding.autoCompleteGameStatus.setText(game.status)
        binding.imageViewGamePreview.setImageURI(game.image?.toUri())
    }

    private fun setupStatusDropdown() {
        val statusOptions = arrayOf("Pending", "Playing", "Completed", "Paused")
        val adapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, statusOptions)
        binding.autoCompleteGameStatus.setAdapter(adapter)
    }

    private fun setupImageSelection() {
        binding.buttonSelectImage.setOnClickListener {
            openGallery()
        }
    }


    private fun openGallery() {
        documentPickerLauncher.launch(arrayOf("image/*"))
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
            updateGame()
        }
    }

    private fun setupCancelButton() {
        binding.buttonCancelEdit.setOnClickListener {
            finish()
        }
    }

    private fun updateGame() {
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
        if (selectedImageUri.isNullOrEmpty()) {
            selectedImageUri = currentGame.image
            }

        // Update game object
        val updatedGame = currentGame.copy(
            name = name,
            genre = genre,
            status = status,
            image = selectedImageUri.toString()
        )

        lifecycleScope.launch {
            try {
                // Update the game
                repository.updateGame(updatedGame)

                // Clear existing player and platform associations
                repository.removeAllPlayersFromGame(currentGame.id)
                repository.removeAllPlatformsFromGame(currentGame.id)

                // Assign selected players to the game
                selectedPlayers.forEach { player ->
                    repository.assignGameToPlayer(currentGame.id, player.id)
                }

                // Assign selected platforms to the game
                selectedPlatforms.forEach { platform ->
                    repository.assignGameToPlatform(currentGame.id, platform.id)
                }

                Toast.makeText(
                    this@EditGameActivity,
                    "Game updated successfully",
                    Toast.LENGTH_LONG
                ).show()

                // Return to previous activity
                setResult(RESULT_OK)
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@EditGameActivity,
                    "Error updating game: ${e.message}",
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