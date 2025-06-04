package com.example.practicavideojuegos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.practicavideojuegos.data.entity.Game
import com.example.practicavideojuegos.data.entity.Player

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY name ASC")
    suspend fun getAllPlayers(): List<Player>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayerById(id: Int): Player?

    @Query("SELECT * FROM players WHERE name LIKE :name")
    suspend fun getPlayersByName(name: String): List<Player>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    // Get games for a player
    @Query("""
        SELECT g.* FROM games g
        INNER JOIN game_player gp ON g.id = gp.gameId
        WHERE gp.playerId = :playerId
        ORDER BY g.name ASC
    """)
    suspend fun getGamesForPlayer(playerId: Int): List<Game>

}