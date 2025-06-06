package com.example.practicavideojuegos.ui.create

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practicavideojuegos.data.entity.Platform
import com.example.practicavideojuegos.data.db.GameDatabase
import com.example.practicavideojuegos.data.entity.Player
import com.example.practicavideojuegos.data.repository.GameRepository
import com.example.practicavideojuegos.databinding.ActivityCreatePlatformBinding
import com.example.practicavideojuegos.databinding.ActivityCreatePlayerBinding
import kotlinx.coroutines.launch

class CreatePlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePlayerBinding
    private lateinit var repository: GameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Player"

        // Initialize repository
        val database = GameDatabase.getDatabase(this)
        repository = GameRepository(database)

        setupSaveButton()
    }

    private fun setupSaveButton() {
        binding.buttonSavePlayer.setOnClickListener {
            savePlatform()
        }
    }

    private fun savePlatform() {
        val playerName = binding.editTextPlayerName.text.toString().trim()

        // Validate input
        if (playerName.isEmpty()) {
            binding.textInputLayoutPlayerName.error = "Platform name is required"
            return
        }

        // Clear any previous errors
        binding.textInputLayoutPlayerName.error = null

        // Create platform object
        val platform = Player(
            id = 0, // Room will auto-generate the ID
            name = playerName
        )

        // Save to database
        lifecycleScope.launch {
            try {
                // Check if platform already exists
                val existingPlayer = repository.getAllPlatforms()
                val playerExists = existingPlayer.any {
                    it.name.equals(playerName, ignoreCase = true)
                }

                if (playerExists) {
                    Toast.makeText(
                        this@CreatePlayerActivity,
                        "Player '$playerName' already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // Insert the platform
                repository.insertPlayer(platform)

                Toast.makeText(
                    this@CreatePlayerActivity,
                    "Player '$playerName' created successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Return to previous screen
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@CreatePlayerActivity,
                    "Error creating player: ${e.message}",
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