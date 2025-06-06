package com.example.practicavideojuegos.ui.create

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practicavideojuegos.data.entity.Platform
import com.example.practicavideojuegos.data.db.GameDatabase
import com.example.practicavideojuegos.data.repository.GameRepository
import com.example.practicavideojuegos.databinding.ActivityCreatePlatformBinding
import kotlinx.coroutines.launch

class CreatePlatformActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePlatformBinding
    private lateinit var repository: GameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePlatformBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Platform"

        // Initialize repository
        val database = GameDatabase.getDatabase(this)
        repository = GameRepository(database)

        setupSaveButton()
    }

    private fun setupSaveButton() {
        binding.buttonSavePlatform.setOnClickListener {
            savePlatform()
        }
    }

    private fun savePlatform() {
        val platformName = binding.editTextPlatformName.text.toString().trim()

        // Validate input
        if (platformName.isEmpty()) {
            binding.textInputLayoutPlatformName.error = "Platform name is required"
            return
        }

        // Clear any previous errors
        binding.textInputLayoutPlatformName.error = null

        // Create platform object
        val platform = Platform(
            id = 0, // Room will auto-generate the ID
            name = platformName
        )

        // Save to database
        lifecycleScope.launch {
            try {
                // Check if platform already exists
                val existingPlatforms = repository.getAllPlatforms()
                val platformExists = existingPlatforms.any {
                    it.name.equals(platformName, ignoreCase = true)
                }

                if (platformExists) {
                    Toast.makeText(
                        this@CreatePlatformActivity,
                        "Platform '$platformName' already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // Insert the platform
                repository.insertPlatform(platform)

                Toast.makeText(
                    this@CreatePlatformActivity,
                    "Platform '$platformName' created successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Return to previous screen
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@CreatePlatformActivity,
                    "Error creating platform: ${e.message}",
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