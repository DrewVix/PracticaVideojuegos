package com.example.practicavideojuegos.data.repository

import com.example.practicavideojuegos.data.db.GameDatabase
import com.example.practicavideojuegos.data.entity.Game
import com.example.practicavideojuegos.data.entity.GamePlatform
import com.example.practicavideojuegos.data.entity.GamePlayer
import com.example.practicavideojuegos.data.entity.Platform
import com.example.practicavideojuegos.data.entity.Player

class GameRepository(private val database: GameDatabase) {

    private val playerDao = database.playerDao()
    private val gameDao = database.gameDao()
    private val platformDao = database.platformDao()
    private val relationshipDao = database.relationshipDao()

    // ===== PLAYER OPERATIONS =====
    suspend fun getAllPlayers() = playerDao.getAllPlayers()
    suspend fun getPlayerById(id: Int) = playerDao.getPlayerById(id)
    suspend fun insertPlayer(player: Player) = playerDao.insertPlayer(player)
    suspend fun updatePlayer(player: Player) = playerDao.updatePlayer(player)
    suspend fun deletePlayer(player: Player) = playerDao.deletePlayer(player)
    suspend fun getGamesForPlayer(playerId: Int) = playerDao.getGamesForPlayer(playerId)

    // ===== GAME OPERATIONS =====
    suspend fun getAllGames() = gameDao.getAllGames()
    suspend fun getGameById(id: Int) = gameDao.getGameById(id)
    suspend fun insertGame(game: Game) = gameDao.insertGame(game)
    suspend fun updateGame(game: Game) = gameDao.updateGame(game)
    suspend fun deleteGame(game: Game) = gameDao.deleteGame(game)
    suspend fun getGamesByStatus(status: String) = gameDao.getGamesByStatus(status)
    suspend fun getGamesByGenre(genre: String) = gameDao.getGamesByGenre(genre)

    // Get related entities for games
    suspend fun getPlayersForGame(gameId: Int) = gameDao.getPlayersForGame(gameId)
    suspend fun getPlatformsForGame(gameId: Int) = gameDao.getPlatformsForGame(gameId)

    // ===== PLATFORM OPERATIONS =====
    suspend fun getAllPlatforms() = platformDao.getAllPlatforms()
    suspend fun getPlatformById(id: Int) = platformDao.getPlatformById(id)
    suspend fun insertPlatform(platform: Platform) = platformDao.insertPlatform(platform)
    suspend fun updatePlatform(platform: Platform) = platformDao.updatePlatform(platform)
    suspend fun deletePlatform(platform: Platform) = platformDao.deletePlatform(platform)
    suspend fun getGamesForPlatform(platformId: Int) = platformDao.getGamesForPlatform(platformId)

    // ===== RELATIONSHIP OPERATIONS =====
    suspend fun assignGameToPlayer(gameId: Int, playerId: Int) {
        relationshipDao.insertGamePlayer(GamePlayer(playerId, gameId))
    }

    suspend fun assignGameToPlatform(gameId: Int, platformId: Int) {
        relationshipDao.insertGamePlatform(GamePlatform(gameId, platformId))
    }

    suspend fun removePlayerFromGame(gameId: Int, playerId: Int) {
        relationshipDao.removePlayerFromGame(gameId, playerId)
    }

    suspend fun removePlatformFromGame(gameId: Int, platformId: Int) {
        relationshipDao.removePlatformFromGame(gameId, platformId)
    }

    // ===== NEW FUNCTIONS FOR EDIT GAME =====
    suspend fun removeAllPlayersFromGame(gameId: Int) {
        relationshipDao.removeAllPlayersFromGame(gameId)
    }

    suspend fun removeAllPlatformsFromGame(gameId: Int) {
        relationshipDao.removeAllPlatformsFromGame(gameId)
    }

    // Get all relationships
    suspend fun getAllGamePlayerRelationships() = relationshipDao.getAllGamePlayerRelationships()
    suspend fun getAllGamePlatformRelationships() = relationshipDao.getAllGamePlatformRelationships()

    // Check if relationships exist
    suspend fun isPlayerAssignedToGame(gameId: Int, playerId: Int) =
        relationshipDao.isPlayerAssignedToGame(gameId, playerId)

    suspend fun isPlatformAssignedToGame(gameId: Int, platformId: Int) =
        relationshipDao.isPlatformAssignedToGame(gameId, platformId)
}