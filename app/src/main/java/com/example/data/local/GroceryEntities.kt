package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class SplitSection {
    RIGHT,
    LEFT
}

@Entity(tableName = "grocery_pages")
data class GroceryPageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val pageName: String,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val pageId: String,
    val section: SplitSection,
    val qty: Int = 1,
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
)

@Entity(tableName = "grocery_suggestions")
data class SuggestionEntity(
    @PrimaryKey
    val name: String,
    val frequency: Int = 1,
    val lastUsed: Long = System.currentTimeMillis()
)
