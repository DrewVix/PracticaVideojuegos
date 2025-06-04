package com.example.practicavideojuegos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.practicavideojuegos.data.entity.Game
import com.example.practicavideojuegos.data.entity.Platform
import com.example.practicavideojuegos.data.entity.Player

@Dao
interface GameDao {
    @Query("SELECT * FROM games")
    suspend fun getAllGames(): List<Game>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Int): Game?

    @Query("SELECT * FROM games WHERE status = :status")
    suspend fun getGamesByStatus(status: String): List<Game>

    @Query("SELECT * FROM games WHERE genre = :genre")
    suspend fun getGamesByGenre(genre: String): List<Game>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game)

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    // Get players for a game
    @Query("""
        SELECT p.* FROM players p
        INNER JOIN game_player gp ON p.id = gp.playerId
        WHERE gp.gameId = :gameId
        ORDER BY p.name ASC
    """)
    suspend fun getPlayersForGame(gameId: Int): List<Player>

    // Get platforms for a game
    @Query("""
        SELECT p.* FROM platforms p
        INNER JOIN game_platform gp ON p.id = gp.platformId
        WHERE gp.gameId = :gameId
        ORDER BY p.name ASC
    """)
    suspend fun getPlatformsForGame(gameId: Int): List<Platform>

}