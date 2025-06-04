package com.example.practicavideojuegos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.practicavideojuegos.data.entity.GamePlatform
import com.example.practicavideojuegos.data.entity.GamePlayer

@Dao
interface RelationshipDao {
    // ===== GAME-PLAYER RELATIONSHIPS =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGamePlayer(gamePlayer: GamePlayer)

    @Delete
    suspend fun deleteGamePlayer(gamePlayer: GamePlayer)

    @Query("DELETE FROM game_player WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun removePlayerFromGame(gameId: Int, playerId: Int)

    @Query("SELECT * FROM game_player")
    suspend fun getAllGamePlayerRelationships(): List<GamePlayer>

    @Query("SELECT * FROM game_player WHERE gameId = :gameId")
    suspend fun getPlayersForGame(gameId: Int): List<GamePlayer>

    @Query("SELECT * FROM game_player WHERE playerId = :playerId")
    suspend fun getGamesForPlayer(playerId: Int): List<GamePlayer>

    @Query("SELECT COUNT(*) > 0 FROM game_player WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun isPlayerAssignedToGame(gameId: Int, playerId: Int): Boolean

    // ===== GAME-PLATFORM RELATIONSHIPS =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGamePlatform(gamePlatform: GamePlatform)

    @Delete
    suspend fun deleteGamePlatform(gamePlatform: GamePlatform)

    @Query("DELETE FROM game_platform WHERE gameId = :gameId AND platformId = :platformId")
    suspend fun removePlatformFromGame(gameId: Int, platformId: Int)

    @Query("SELECT * FROM game_platform")
    suspend fun getAllGamePlatformRelationships(): List<GamePlatform>

    @Query("SELECT * FROM game_platform WHERE gameId = :gameId")
    suspend fun getPlatformsForGame(gameId: Int): List<GamePlatform>

    @Query("SELECT * FROM game_platform WHERE platformId = :platformId")
    suspend fun getGamesForPlatform(platformId: Int): List<GamePlatform>

    @Query("SELECT COUNT(*) > 0 FROM game_platform WHERE gameId = :gameId AND platformId = :platformId")
    suspend fun isPlatformAssignedToGame(gameId: Int, platformId: Int): Boolean

    // ===== BULK OPERATIONS =====
    @Query("DELETE FROM game_player WHERE gameId = :gameId")
    suspend fun removeAllPlayersFromGame(gameId: Int)

    @Query("DELETE FROM game_platform WHERE gameId = :gameId")
    suspend fun removeAllPlatformsFromGame(gameId: Int)

    @Query("DELETE FROM game_player WHERE playerId = :playerId")
    suspend fun removePlayerFromAllGames(playerId: Int)

    @Query("DELETE FROM game_platform WHERE platformId = :platformId")
    suspend fun removePlatformFromAllGames(platformId: Int)
}
