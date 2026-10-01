package com.example.ui

import com.example.data.local.GroceryItemEntity
import com.example.data.local.GroceryPageEntity
import com.example.data.local.SplitSection
import com.example.data.local.SuggestionEntity
import com.example.domain.SpeechTargetField

data class GroceryUiState(
    val pages: List<GroceryPageEntity> = emptyList(),
    val currentPageId: String? = null,
    val currentPageName: String = "صفحة 1",
    val rightItems: List<GroceryItemEntity> = emptyList(),
    val leftItems: List<GroceryItemEntity> = emptyList(),

    // Input Fields
    val inputQty: String = "1",
    val inputName: String = "",
    val targetSection: SplitSection = SplitSection.RIGHT,
    val autoMerge: Boolean = true,
    val fontSizeSp: Float = 12f,

    // Autocomplete Suggestions
    val suggestions: List<SuggestionEntity> = emptyList(),
    val filteredSuggestions: List<SuggestionEntity> = emptyList(),

    // Voice State
    val isListening: Boolean = false,
    val listeningTarget: SpeechTargetField = SpeechTargetField.MASTER,
    val audioRms: Float = 0f,
    val lastSpokenText: String? = null,

    // Dialogs & Feedback
    val toastMessage: String? = null,
    val showExportDialog: Boolean = false,
    val showClearConfirmDialog: Boolean = false,
    val showNewPageDialog: Boolean = false,
    val showRenamePageDialog: Boolean = false,
    val editingItem: GroceryItemEntity? = null,
    val isLandscapeLocked: Boolean = false
) {
    val totalItemsCount: Int
        get() = rightItems.size + leftItems.size

    val rightItemsCount: Int
        get() = rightItems.size

    val leftItemsCount: Int
        get() = leftItems.size
}
