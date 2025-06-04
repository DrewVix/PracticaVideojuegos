package com.example.practicavideojuegos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.practicavideojuegos.data.entity.Game
import com.example.practicavideojuegos.data.entity.Platform

@Dao
interface PlatformDao {
    @Query("SELECT * FROM platforms ORDER BY name ASC")
    suspend fun getAllPlatforms(): List<Platform>

    @Query("SELECT * FROM platforms WHERE id = :id")
    suspend fun getPlatformById(id: Int): Platform?

    @Query("SELECT * FROM platforms WHERE name LIKE :name")
    suspend fun getPlatformsByName(name: String): List<Platform>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlatform(platform: Platform)

    @Update
    suspend fun updatePlatform(platform: Platform)

    @Delete
    suspend fun deletePlatform(platform: Platform)

    // Get games for a platform
    @Query("""
        SELECT g.* FROM games g
        INNER JOIN game_platform gp ON g.id = gp.gameId
        WHERE gp.platformId = :platformId
        ORDER BY g.name ASC
    """)
    suspend fun getGamesForPlatform(platformId: Int): List<Game>

}