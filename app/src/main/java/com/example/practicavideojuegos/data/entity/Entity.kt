package com.example.practicavideojuegos.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey val id: Int,
    val name: String
)

@Entity(tableName = "games")
data class Game(
    @PrimaryKey val id: Int,
    val name: String,
    val status: String,
    val genre: String,
    val image: Int
)

@Entity(tableName = "platforms")
data class Platform(
    @PrimaryKey val id: Int,
    val name: String
)

// 2. RELATIONSHIP TABLES (Many-to-Many)

@Entity(
    tableName = "game_player",
    primaryKeys = ["playerId", "gameId"],
    foreignKeys = [
        ForeignKey(
            entity = Player::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GamePlayer(
    val playerId: Int,
    val gameId: Int
)

@Entity(
    tableName = "game_platform",
    primaryKeys = ["gameId", "platformId"],
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Platform::class,
            parentColumns = ["id"],
            childColumns = ["platformId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GamePlatform(
    val gameId: Int,
    val platformId: Int
)
