package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {

    // --- Pages ---
    @Query("SELECT * FROM grocery_pages ORDER BY orderIndex ASC, createdAt ASC")
    fun getAllPages(): Flow<List<GroceryPageEntity>>

    @Query("SELECT * FROM grocery_pages WHERE id = :id LIMIT 1")
    suspend fun getPageById(id: String): GroceryPageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: GroceryPageEntity)

    @Update
    suspend fun updatePage(page: GroceryPageEntity)

    @Query("DELETE FROM grocery_pages WHERE id = :pageId")
    suspend fun deletePage(pageId: String)

    // --- Items ---
    @Query("SELECT * FROM grocery_items WHERE pageId = :pageId ORDER BY orderIndex ASC, timestamp ASC")
    fun getItemsForPage(pageId: String): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE pageId = :pageId AND section = :section ORDER BY orderIndex ASC, timestamp ASC")
    fun getItemsForSection(pageId: String, section: SplitSection): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE pageId = :pageId AND section = :section AND LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun findItemByName(pageId: String, section: SplitSection, name: String): GroceryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GroceryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<GroceryItemEntity>)

    @Update
    suspend fun updateItem(item: GroceryItemEntity)

    @Query("DELETE FROM grocery_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM grocery_items WHERE pageId = :pageId")
    suspend fun clearPageItems(pageId: String)

    @Query("DELETE FROM grocery_items WHERE pageId = :pageId AND section = :section")
    suspend fun clearSectionItems(pageId: String, section: SplitSection)

    // --- Suggestions ---
    @Query("SELECT * FROM grocery_suggestions ORDER BY frequency DESC, lastUsed DESC LIMIT 25")
    fun getAllSuggestions(): Flow<List<SuggestionEntity>>

    @Query("SELECT * FROM grocery_suggestions WHERE name LIKE '%' || :query || '%' ORDER BY frequency DESC, lastUsed DESC LIMIT 15")
    fun searchSuggestions(query: String): Flow<List<SuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: SuggestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestions(suggestions: List<SuggestionEntity>)
}
