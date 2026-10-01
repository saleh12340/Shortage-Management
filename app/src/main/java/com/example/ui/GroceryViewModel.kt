package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GroceryDatabase
import com.example.data.local.GroceryItemEntity
import com.example.data.local.GroceryRepository
import com.example.data.local.SplitSection
import com.example.domain.CommandType
import com.example.domain.SpeechRecognizerManager
import com.example.domain.SpeechTargetField
import com.example.domain.VoiceParseResult
import com.example.domain.VoiceParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroceryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GroceryRepository
    val speechManager: SpeechRecognizerManager

    private val _uiState = MutableStateFlow(GroceryUiState())
    val uiState: StateFlow<GroceryUiState> = _uiState.asStateFlow()

    init {
        val database = GroceryDatabase.getInstance(application)
        repository = GroceryRepository(database.groceryDao())
        speechManager = SpeechRecognizerManager(application)

        // Observe speech recognition events
        speechManager.onSpeechResult = { text, target ->
            handleVoiceResult(text, target)
        }

        viewModelScope.launch {
            speechManager.isListening.collectLatest { listening ->
                _uiState.update { it.copy(isListening = listening) }
            }
        }

        viewModelScope.launch {
            speechManager.audioRms.collectLatest { rms ->
                _uiState.update { it.copy(audioRms = rms) }
            }
        }

        viewModelScope.launch {
            speechManager.currentTarget.collectLatest { target ->
                _uiState.update { it.copy(listeningTarget = target) }
            }
        }

        viewModelScope.launch {
            speechManager.errorMessage.collectLatest { err ->
                if (err != null) {
                    showToast(err)
                    speechManager.clearError()
                }
            }
        }

        // Observe Pages
        viewModelScope.launch {
            repository.pages.collectLatest { pageList ->
                if (pageList.isEmpty()) {
                    // Create initial page if DB just created
                    val newId = repository.createPage("صفحة 1")
                    _uiState.update {
                        it.copy(
                            pages = emptyList(),
                            currentPageId = newId,
                            currentPageName = "صفحة 1"
                        )
                    }
                } else {
                    val currentId = _uiState.value.currentPageId
                    val matchedPage = pageList.find { it.id == currentId } ?: pageList.first()
                    _uiState.update {
                        it.copy(
                            pages = pageList,
                            currentPageId = matchedPage.id,
                            currentPageName = matchedPage.pageName
                        )
                    }
                    observeItemsForPage(matchedPage.id)
                }
            }
        }

        // Observe Autocomplete Suggestions
        viewModelScope.launch {
            repository.suggestions.collectLatest { suggList ->
                _uiState.update {
                    it.copy(
                        suggestions = suggList,
                        filteredSuggestions = suggList.take(6)
                    )
                }
            }
        }
    }

    private var itemsJob: kotlinx.coroutines.Job? = null

    private fun observeItemsForPage(pageId: String) {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            repository.getItemsForPage(pageId).collectLatest { items ->
                val right = items.filter { it.section == SplitSection.RIGHT }
                val left = items.filter { it.section == SplitSection.LEFT }
                _uiState.update {
                    it.copy(
                        rightItems = right,
                        leftItems = left
                    )
                }
            }
        }
    }

    fun selectPage(pageId: String) {
        val page = _uiState.value.pages.find { it.id == pageId } ?: return
        _uiState.update {
            it.copy(
                currentPageId = page.id,
                currentPageName = page.pageName
            )
        }
        observeItemsForPage(page.id)
    }

    fun createPage(pageName: String) {
        val name = if (pageName.isBlank()) "صفحة ${_uiState.value.pages.size + 1}" else pageName.trim()
        viewModelScope.launch {
            val newId = repository.createPage(name)
            selectPage(newId)
            showToast("تم إنشاء $name")
        }
    }

    fun renameCurrentPage(newName: String) {
        val pageId = _uiState.value.currentPageId ?: return
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.renamePage(pageId, trimmed)
            _uiState.update { it.copy(currentPageName = trimmed) }
            showToast("تم إعادة تسمية الصفحة إلى $trimmed")
        }
    }

    fun deleteCurrentPage() {
        val pageId = _uiState.value.currentPageId ?: return
        if (_uiState.value.pages.size <= 1) {
            showToast("لا يمكن حذف الصفحة الوحيدة")
            return
        }
        viewModelScope.launch {
            repository.deletePage(pageId)
            showToast("تم حذف الصفحة")
        }
    }

    fun onInputQtyChange(qty: String) {
        val filtered = qty.filter { it.isDigit() }
        _uiState.update { it.copy(inputQty = filtered) }
    }

    fun incrementQty() {
        val current = _uiState.value.inputQty.toIntOrNull() ?: 1
        _uiState.update { it.copy(inputQty = (current + 1).toString()) }
    }

    fun decrementQty() {
        val current = _uiState.value.inputQty.toIntOrNull() ?: 1
        val next = (current - 1).coerceAtLeast(1)
        _uiState.update { it.copy(inputQty = next.toString()) }
    }

    fun onInputNameChange(name: String) {
        _uiState.update { state ->
            val filtered = if (name.isBlank()) {
                state.suggestions.take(6)
            } else {
                state.suggestions.filter { it.name.contains(name, ignoreCase = true) }.take(6)
            }
            state.copy(inputName = name, filteredSuggestions = filtered)
        }
    }

    fun onSuggestionSelected(suggestion: String) {
        _uiState.update { it.copy(inputName = suggestion) }
    }

    fun setTargetSection(section: SplitSection) {
        _uiState.update { it.copy(targetSection = section) }
    }

    fun toggleAutoMerge() {
        _uiState.update { it.copy(autoMerge = !it.autoMerge) }
    }

    fun addItem() {
        val state = _uiState.value
        val name = state.inputName.trim()
        if (name.isEmpty()) {
            showToast("يرجى كتابة اسم الصنف أولاً")
            return
        }
        val qty = state.inputQty.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val pageId = state.currentPageId ?: return

        viewModelScope.launch {
            repository.addItem(
                pageId = pageId,
                section = state.targetSection,
                name = name,
                qty = qty,
                autoMerge = state.autoMerge
            )
            _uiState.update {
                it.copy(
                    inputName = "",
                    inputQty = "1"
                )
            }
            showToast("تمت إضافة: $name ($qty)")
        }
    }

    fun updateItem(item: GroceryItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
            showToast("تم حذف الصنف")
        }
    }

    fun moveItemToOtherSplit(item: GroceryItemEntity) {
        viewModelScope.launch {
            repository.moveItemToOtherSplit(item)
            val destName = if (item.section == SplitSection.RIGHT) "الشق الأيسر 🟢" else "الشق الأيمن 🔷"
            showToast("تم نقل الصنف إلى $destName")
        }
    }

    fun swapSplits() {
        val pageId = _uiState.value.currentPageId ?: return
        val allItems = _uiState.value.rightItems + _uiState.value.leftItems
        if (allItems.isEmpty()) {
            showToast("لا توجد أصناف لقلبها")
            return
        }
        viewModelScope.launch {
            repository.swapSplits(pageId, allItems)
            showToast("تم قلب الشقين الأيمن والأيسر بنجاح 🔄")
        }
    }

    fun clearCurrentPage() {
        val pageId = _uiState.value.currentPageId ?: return
        viewModelScope.launch {
            repository.clearPage(pageId)
            showToast("تم مسح أصناف الصفحة الحالية")
        }
    }

    fun increaseFontSize() {
        _uiState.update { it.copy(fontSizeSp = (it.fontSizeSp + 1f).coerceAtMost(18f)) }
    }

    fun decreaseFontSize() {
        _uiState.update { it.copy(fontSizeSp = (it.fontSizeSp - 1f).coerceAtLeast(10f)) }
    }

    fun handleVoiceResult(spokenText: String, target: SpeechTargetField) {
        _uiState.update { it.copy(lastSpokenText = spokenText) }

        when (target) {
            SpeechTargetField.QUANTITY_ONLY -> {
                val parsedQty = VoiceParser.parseQuantityOnly(spokenText)
                if (parsedQty != null) {
                    _uiState.update { it.copy(inputQty = parsedQty.toString()) }
                    showToast("تم تحديد العدد: $parsedQty")
                } else {
                    showToast("لم يتم تمييز رقم، نطق: $spokenText")
                }
            }

            SpeechTargetField.NAME_ONLY -> {
                _uiState.update { it.copy(inputName = spokenText) }
                showToast("تم تحديد الصنف: $spokenText")
            }

            SpeechTargetField.MASTER -> {
                val result = VoiceParser.parseSpokenSentence(spokenText)
                when (result) {
                    is VoiceParseResult.Command -> {
                        when (result.type) {
                            CommandType.PRINT -> {
                                openExportDialog()
                                showToast("أمر صوتي: فتح خيارات الطباعة")
                            }
                            CommandType.COPY -> {
                                showToast("أمر صوتي: نسخ القائمة")
                            }
                            CommandType.SWAP -> {
                                swapSplits()
                            }
                            CommandType.CLEAR -> {
                                _uiState.update { it.copy(showClearConfirmDialog = true) }
                            }
                            CommandType.NEW_PAGE -> {
                                createPage("صفحة ${_uiState.value.pages.size + 1}")
                            }
                        }
                    }

                    is VoiceParseResult.AddItem -> {
                        val targetSplit = result.targetSection ?: _uiState.value.targetSection
                        val pageId = _uiState.value.currentPageId ?: return
                        viewModelScope.launch {
                            repository.addItem(
                                pageId = pageId,
                                section = targetSplit,
                                name = result.name,
                                qty = result.qty,
                                autoMerge = _uiState.value.autoMerge
                            )
                            val splitLabel = if (targetSplit == SplitSection.RIGHT) "الشق الأيمن 🔷" else "الشق الأيسر 🟢"
                            showToast("تمت الإضافة ($splitLabel): ${result.name} (${result.qty})")
                        }
                    }

                    is VoiceParseResult.QuantityOnly -> {
                        _uiState.update { it.copy(inputQty = result.qty.toString()) }
                        showToast("تم تسجيل العدد: ${result.qty}")
                    }

                    is VoiceParseResult.NameOnly -> {
                        _uiState.update { it.copy(inputName = result.name) }
                        showToast("تم تسجيل الصنف: ${result.name}")
                    }

                    VoiceParseResult.Unrecognized -> {
                        showToast("تم الاستماع: $spokenText")
                    }
                }
            }
        }
    }

    fun startListening(target: SpeechTargetField = SpeechTargetField.MASTER) {
        speechManager.startListening(target)
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun openExportDialog() {
        _uiState.update { it.copy(showExportDialog = true) }
    }

    fun closeExportDialog() {
        _uiState.update { it.copy(showExportDialog = false) }
    }

    fun openClearConfirmDialog() {
        _uiState.update { it.copy(showClearConfirmDialog = true) }
    }

    fun closeClearConfirmDialog() {
        _uiState.update { it.copy(showClearConfirmDialog = false) }
    }

    fun openNewPageDialog() {
        _uiState.update { it.copy(showNewPageDialog = true) }
    }

    fun closeNewPageDialog() {
        _uiState.update { it.copy(showNewPageDialog = false) }
    }

    fun openRenamePageDialog() {
        _uiState.update { it.copy(showRenamePageDialog = true) }
    }

    fun closeRenamePageDialog() {
        _uiState.update { it.copy(showRenamePageDialog = false) }
    }

    fun startEditingItem(item: GroceryItemEntity) {
        _uiState.update { it.copy(editingItem = item) }
    }

    fun cancelEditingItem() {
        _uiState.update { it.copy(editingItem = null) }
    }

    fun saveEditedItem(updated: GroceryItemEntity) {
        updateItem(updated)
        _uiState.update { it.copy(editingItem = null) }
        showToast("تم تحديث الصنف")
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
    }
}
