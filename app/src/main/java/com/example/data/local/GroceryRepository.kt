package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class GroceryRepository(private val dao: GroceryDao) {

    val pages: Flow<List<GroceryPageEntity>> = dao.getAllPages()
    val suggestions: Flow<List<SuggestionEntity>> = dao.getAllSuggestions()

    fun getItemsForPage(pageId: String): Flow<List<GroceryItemEntity>> =
        dao.getItemsForPage(pageId)

    fun searchSuggestions(query: String): Flow<List<SuggestionEntity>> =
        dao.searchSuggestions(query)

    suspend fun createPage(name: String): String = withContext(Dispatchers.IO) {
        val newId = UUID.randomUUID().toString()
        val page = GroceryPageEntity(id = newId, pageName = name)
        dao.insertPage(page)
        newId
    }

    suspend fun renamePage(pageId: String, newName: String) = withContext(Dispatchers.IO) {
        val page = dao.getPageById(pageId)
        if (page != null) {
            dao.updatePage(page.copy(pageName = newName))
        }
    }

    suspend fun deletePage(pageId: String) = withContext(Dispatchers.IO) {
        dao.clearPageItems(pageId)
        dao.deletePage(pageId)
    }

    suspend fun addItem(
        pageId: String,
        section: SplitSection,
        name: String,
        qty: Int,
        autoMerge: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return@withContext false

        // Record suggestion
        val existingSuggestion = try {
            dao.searchSuggestions(trimmedName)
        } catch (_: Exception) { null }
        dao.insertSuggestion(
            SuggestionEntity(
                name = trimmedName,
                frequency = 1,
                lastUsed = System.currentTimeMillis()
            )
        )

        if (autoMerge) {
            val existingItem = dao.findItemByName(pageId, section, trimmedName)
            if (existingItem != null) {
                val updated = existingItem.copy(
                    qty = existingItem.qty + qty,
                    timestamp = System.currentTimeMillis()
                )
                dao.updateItem(updated)
                return@withContext true
            }
        }

        val newItem = GroceryItemEntity(
            pageId = pageId,
            section = section,
            qty = qty,
            name = trimmedName,
            timestamp = System.currentTimeMillis()
        )
        dao.insertItem(newItem)
        true
    }

    suspend fun updateItem(item: GroceryItemEntity) = withContext(Dispatchers.IO) {
        dao.updateItem(item)
    }

    suspend fun deleteItem(itemId: String) = withContext(Dispatchers.IO) {
        dao.deleteItem(itemId)
    }

    suspend fun clearPage(pageId: String) = withContext(Dispatchers.IO) {
        dao.clearPageItems(pageId)
    }

    suspend fun swapSplits(pageId: String, currentItems: List<GroceryItemEntity>) = withContext(Dispatchers.IO) {
        val swapped = currentItems.map { item ->
            val newSection = if (item.section == SplitSection.RIGHT) SplitSection.LEFT else SplitSection.RIGHT
            item.copy(section = newSection)
        }
        dao.insertItems(swapped)
    }

    suspend fun moveItemToOtherSplit(item: GroceryItemEntity) = withContext(Dispatchers.IO) {
        val newSection = if (item.section == SplitSection.RIGHT) SplitSection.LEFT else SplitSection.RIGHT
        dao.updateItem(item.copy(section = newSection))
    }
}
