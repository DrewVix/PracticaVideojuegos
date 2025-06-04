package com.example.practicavideojuegos.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.practicavideojuegos.data.dao.GameDao
import com.example.practicavideojuegos.data.dao.PlatformDao
import com.example.practicavideojuegos.data.dao.PlayerDao
import com.example.practicavideojuegos.data.dao.RelationshipDao
import com.example.practicavideojuegos.data.entity.Game
import com.example.practicavideojuegos.data.entity.GamePlatform
import com.example.practicavideojuegos.data.entity.GamePlayer
import com.example.practicavideojuegos.data.entity.Platform
import com.example.practicavideojuegos.data.entity.Player

@Database(
    entities = [
        Player::class,
        Game::class,
        Platform::class,
        GamePlayer::class,
        GamePlatform::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun platformDao(): PlatformDao
    abstract fun relationshipDao(): RelationshipDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}