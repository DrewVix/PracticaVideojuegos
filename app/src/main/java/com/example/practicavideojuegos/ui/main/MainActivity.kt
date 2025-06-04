package com.example.practicavideojuegos.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practicavideojuegos.R
import com.example.practicavideojuegos.data.db.GameDatabase
import com.example.practicavideojuegos.data.entity.*
import com.example.practicavideojuegos.data.repository.GameRepository
import com.example.practicavideojuegos.databinding.ActivityMainBinding
import com.example.practicavideojuegos.ui.create.CreateGameActivity
import com.example.practicavideojuegos.ui.create.CreatePlatformActivity
import com.example.practicavideojuegos.ui.create.CreatePlayerActivity
import com.example.practicavideojuegos.ui.detail.GameDetailActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var gameAdapter: GameAdapter
    private lateinit var repository: GameRepository

    companion object {
        const val EXTRA_GAME_ID = "extra_game_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFab()

        val database = GameDatabase.getDatabase(this)
        repository = GameRepository(database)

        // Insertar datos de prueba y luego cargar
        insertTestDataAndLoad()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        gameAdapter = GameAdapter { game ->
            val intent = Intent(this, GameDetailActivity::class.java)
            intent.putExtra(EXTRA_GAME_ID, game.id)
            startActivity(intent)
        }

        binding.recyclerViewGames.apply {
            adapter = gameAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showCreateOptionsDialog()
        }
    }

    private fun showCreateOptionsDialog() {
        val options = arrayOf("Create Game", "Create Player", "Create Platform")

        AlertDialog.Builder(this)
            .setTitle("Create New")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, CreateGameActivity::class.java))
                    1 -> startActivity(Intent(this, CreatePlayerActivity::class.java))
                    2 -> startActivity(Intent(this, CreatePlatformActivity::class.java))
                }
            }
            .show()
    }

    private fun showGameDetails(game: Game) {
        AlertDialog.Builder(this)
            .setTitle(game.name)
            .setMessage("Genre: ${game.genre}\nStatus: ${game.status}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun insertTestDataAndLoad() {
        lifecycleScope.launch {
            // Insert test data
            insertTestData()
            // Load games after inserting test data
            loadGames()
        }
    }

    private suspend fun insertTestData() {
        // Check if data already exists (avoid duplicates)
        val existingGames = repository.getAllGames()
        if (existingGames.isNotEmpty()) {
            return // Data already exists, don't insert again
        }

        // Insert test players
        val testPlayers = listOf(
            Player(1, "John Doe"),
            Player(2, "Jane Smith"),
            Player(3, "Mike Johnson"),
            Player(4, "Sarah Wilson")
        )

        testPlayers.forEach { player ->
            repository.insertPlayer(player)
        }

        // Insert test platforms
        val testPlatforms = listOf(
            Platform(1, "Nintendo Switch"),
            Platform(2, "PlayStation 5"),
            Platform(3, "Xbox Series X"),
            Platform(4, "PC Steam"),
            Platform(5, "Mobile iOS"),
            Platform(6, "Mobile Android")
        )

        testPlatforms.forEach { platform ->
            repository.insertPlatform(platform)
        }

        // Insert test games
        val testGames = listOf(
            Game(1, "The Legend of Zelda: Breath of the Wild", "Completed", "Adventure", R.drawable.image_placeholder_background),
            Game(2, "Super Mario Odyssey", "Playing", "Platform", R.drawable.image_placeholder_background),
            Game(3, "Cyberpunk 2077", "Pending", "RPG", R.drawable.image_placeholder_background),
            Game(4, "FIFA 24", "Playing", "Sports", R.drawable.image_placeholder_background),
            Game(5, "Call of Duty: Modern Warfare III", "Completed", "FPS", R.drawable.image_placeholder_background),
            Game(6, "Minecraft", "Playing", "Sandbox", R.drawable.image_placeholder_background),
            Game(7, "Grand Theft Auto V", "Completed", "Action", R.drawable.image_placeholder_background),
            Game(8, "Among Us", "Paused", "Social Deduction", R.drawable.image_placeholder_background),
            Game(9, "Fortnite", "Playing", "Battle Royale", R.drawable.image_placeholder_background),
            Game(10, "The Witcher 3: Wild Hunt", "Pending", "RPG", R.drawable.image_placeholder_background)
        )

        testGames.forEach { game ->
            repository.insertGame(game)
        }

        // Create relationships between games and players
        val gamePlayerRelationships = listOf(
            // John Doe's games
            Pair(1, 1), // Zelda
            Pair(2, 1), // Mario
            Pair(6, 1), // Minecraft

            // Jane Smith's games
            Pair(3, 2), // Cyberpunk
            Pair(4, 2), // FIFA
            Pair(9, 2), // Fortnite

            // Mike Johnson's games
            Pair(5, 3), // Call of Duty
            Pair(7, 3), // GTA V
            Pair(8, 3), // Among Us

            // Sarah Wilson's games
            Pair(1, 4), // Zelda (shared with John)
            Pair(10, 4), // Witcher 3
            Pair(6, 4)  // Minecraft (shared with John)
        )

        gamePlayerRelationships.forEach { (gameId, playerId) ->
            repository.assignGameToPlayer(gameId, playerId)
        }

        // Create relationships between games and platforms
        val gamePlatformRelationships = listOf(
            // Zelda - Nintendo Switch
            Pair(1, 1),
            // Mario - Nintendo Switch
            Pair(2, 1),
            // Cyberpunk - PC, PS5, Xbox
            Pair(3, 2), Pair(3, 3), Pair(3, 4),
            // FIFA - PS5, Xbox, PC
            Pair(4, 2), Pair(4, 3), Pair(4, 4),
            // Call of Duty - PS5, Xbox, PC
            Pair(5, 2), Pair(5, 3), Pair(5, 4),
            // Minecraft - All platforms
            Pair(6, 1), Pair(6, 2), Pair(6, 3), Pair(6, 4), Pair(6, 5), Pair(6, 6),
            // GTA V - PS5, Xbox, PC
            Pair(7, 2), Pair(7, 3), Pair(7, 4),
            // Among Us - Mobile, PC, Nintendo Switch
            Pair(8, 1), Pair(8, 4), Pair(8, 5), Pair(8, 6),
            // Fortnite - All platforms except some
            Pair(9, 1), Pair(9, 2), Pair(9, 3), Pair(9, 4), Pair(9, 5), Pair(9, 6),
            // Witcher 3 - PC, PS5, Xbox, Switch
            Pair(10, 1), Pair(10, 2), Pair(10, 3), Pair(10, 4)
        )

        gamePlatformRelationships.forEach { (gameId, platformId) ->
            repository.assignGameToPlatform(gameId, platformId)
        }
    }

    private fun loadGames() {
        lifecycleScope.launch {
            val games = repository.getAllGames()
            gameAdapter.submitList(games)
        }
    }

    override fun onResume() {
        super.onResume()
        loadGames() // Reload when returning from create activities
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_create_game -> {
                startActivity(Intent(this, CreateGameActivity::class.java))
                true
            }
            R.id.menu_create_player -> {
                startActivity(Intent(this, CreatePlayerActivity::class.java))
                true
            }
            R.id.menu_create_platform -> {
                startActivity(Intent(this, CreatePlatformActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}