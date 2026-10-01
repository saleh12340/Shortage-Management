package com.example

import android.app.Activity
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.EscPosGenerator
import com.example.domain.ExportFormatters
import com.example.domain.PrintHelper
import com.example.domain.SpeechTargetField
import com.example.ui.GroceryViewModel
import com.example.ui.components.ClearConfirmDialog
import com.example.ui.components.DualSplitGrid
import com.example.ui.components.ExplicitInputBar
import com.example.ui.components.ExportPrintDialog
import com.example.ui.components.HeaderBar
import com.example.ui.components.InPlaceEditDialog
import com.example.ui.components.NewPageDialog
import com.example.ui.components.PageSwitcherBar
import com.example.ui.components.RenamePageDialog
import com.example.ui.components.VoiceModeBar
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PosAmber
import com.example.ui.theme.PosCardBorder
import com.example.ui.theme.PosDarkBackground
import com.example.ui.theme.PosTextPrimary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Enforce RTL Layout for Arabic Grocery POS
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    GroceryAppMainScreen()
                }
            }
        }
    }
}

@Composable
fun GroceryAppMainScreen(
    viewModel: GroceryViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Permission launcher for Audio Recording
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening(SpeechTargetField.MASTER)
        } else {
            viewModel.showToast("يرجى منح إذن الميكروفون لاستخدام الإدخال الصوتي")
        }
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            val bitmap = EscPosGenerator.create58mmReceiptBitmap(uiState.currentPageName, uiState.rightItems, uiState.leftItems)
            PrintHelper.printBluetoothReceipt(context, bitmap, uiState.currentPageName) { message -> viewModel.showToast(message) }
        } else viewModel.showToast("يلزم السماح بصلاحية Bluetooth للطباعة")
    }

    fun printBluetooth() {
        val permissions = if (Build.VERSION.SDK_INT >= 31) arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN) else emptyArray()
        if (permissions.isNotEmpty() && permissions.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED })
            bluetoothPermissionLauncher.launch(permissions)
        else {
            val bitmap = EscPosGenerator.create58mmReceiptBitmap(uiState.currentPageName, uiState.rightItems, uiState.leftItems)
            PrintHelper.printBluetoothReceipt(context, bitmap, uiState.currentPageName) { message -> viewModel.showToast(message) }
        }
    }
    // Function to check permission before speech recognition
    fun requestAndStartListening(target: SpeechTargetField) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.startListening(target)
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto clear transient toast message
    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            delay(3200)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PosDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Compact Header Bar
                // 1. Compact Header Bar with Dedicated Action Buttons
                HeaderBar(
                    storeTitle = "بقالة العزي للمواد الغذائية",
                    pageName = uiState.currentPageName,
                    totalCount = uiState.totalItemsCount,
                    onDirectPrint = { PrintHelper.printThermalReceipt(context as Activity, EscPosGenerator.create58mmReceiptBitmap(uiState.currentPageName, uiState.rightItems, uiState.leftItems), uiState.currentPageName) },
                    onShareImage = {
                        PrintHelper.shareDualColumnAsImage(
                            context,
                            uiState.currentPageName,
                            uiState.rightItems,
                            uiState.leftItems
                        )
                        viewModel.showToast("جارِ تجهيز ومشاركة صورة الكشف للواتساب...")
                    },
                    onSwapSplits = { viewModel.swapSplits() },
                    onClearScreen = { viewModel.openClearConfirmDialog() },
                )

                // 2. Smart Voice & Mode Bar
                VoiceModeBar(
                    isListening = uiState.isListening,
                    listeningTarget = uiState.listeningTarget,
                    audioRms = uiState.audioRms,
                    targetSection = uiState.targetSection,
                    autoMerge = uiState.autoMerge,
                    lastSpokenText = uiState.lastSpokenText,
                    onStartListening = { target -> requestAndStartListening(target) },
                    onStopListening = { viewModel.stopListening() },
                    onToggleTargetSection = {
                        val newTarget = if (uiState.targetSection == com.example.data.local.SplitSection.RIGHT)
                            com.example.data.local.SplitSection.LEFT else com.example.data.local.SplitSection.RIGHT
                        viewModel.setTargetSection(newTarget)
                    },
                    onToggleAutoMerge = { viewModel.toggleAutoMerge() }
                )

                // 3. Explicit Input Grid
                ExplicitInputBar(
                    qty = uiState.inputQty,
                    name = uiState.inputName,
                    targetSection = uiState.targetSection,
                    suggestions = uiState.filteredSuggestions,
                    isListening = uiState.isListening,
                    listeningTarget = uiState.listeningTarget,
                    onQtyChange = { viewModel.onInputQtyChange(it) },
                    onIncrementQty = { viewModel.incrementQty() },
                    onDecrementQty = { viewModel.decrementQty() },
                    onNameChange = { viewModel.onInputNameChange(it) },
                    onSuggestionSelect = { viewModel.onSuggestionSelected(it) },
                    onAddItem = { viewModel.addItem() },
                    onStartListening = { target -> requestAndStartListening(target) },
                    onStopListening = { viewModel.stopListening() }
                )

                // 4. Navigation & Page Switcher Bar
                PageSwitcherBar(
                    pages = uiState.pages,
                    currentPageId = uiState.currentPageId,
                    fontSizeSp = uiState.fontSizeSp,
                    onSelectPage = { viewModel.selectPage(it) },
                    onOpenNewPageDialog = { viewModel.openNewPageDialog() },
                    onOpenRenamePageDialog = { viewModel.openRenamePageDialog() },
                    onIncreaseFontSize = { viewModel.increaseFontSize() },
                    onDecreaseFontSize = { viewModel.decreaseFontSize() },
                    onCopyList = {
                        val formatted = ExportFormatters.generateMandatoryPreambleText(
                            uiState.currentPageName,
                            uiState.rightItems,
                            uiState.leftItems
                        )
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("نواقص بقالة العزي", formatted)
                        clipboard.setPrimaryClip(clip)
                        viewModel.showToast("تم نسخ كشف النواقص مع الترويسة للحافظة 📋")
                    }
                )

                // 5. Dual Split Side-by-Side Grid (50% - 50% Always)
                DualSplitGrid(
                    rightItems = uiState.rightItems,
                    leftItems = uiState.leftItems,
                    fontSizeSp = uiState.fontSizeSp,
                    onItemClick = { item -> viewModel.startEditingItem(item) },
                    onMoveItem = { item -> viewModel.moveItemToOtherSplit(item) },
                    onDeleteItem = { itemId -> viewModel.deleteItem(itemId) },
                    modifier = Modifier.weight(1f)
                )
            }

            // In-place Item Edit Dialog
            uiState.editingItem?.let { itemToEdit ->
                InPlaceEditDialog(
                    item = itemToEdit,
                    onSave = { updated -> viewModel.saveEditedItem(updated) },
                    onDelete = { itemId -> viewModel.deleteItem(itemId) },
                    onDismiss = { viewModel.cancelEditingItem() }
                )
            }

            // Print & Export Dialog
            if (uiState.showExportDialog) {
                ExportPrintDialog(
                    pageName = uiState.currentPageName,
                    rightItems = uiState.rightItems,
                    leftItems = uiState.leftItems,
                    onDismiss = { viewModel.closeExportDialog() },
                    onShowToast = { msg -> viewModel.showToast(msg) }
                )
            }

            // New Page Dialog
            if (uiState.showNewPageDialog) {
                NewPageDialog(
                    initialName = "صفحة ${uiState.pages.size + 1}",
                    onConfirm = { name ->
                        viewModel.createPage(name)
                        viewModel.closeNewPageDialog()
                    },
                    onDismiss = { viewModel.closeNewPageDialog() }
                )
            }

            // Rename / Manage Page Dialog
            if (uiState.showRenamePageDialog) {
                RenamePageDialog(
                    currentPageName = uiState.currentPageName,
                    onRename = { newName ->
                        viewModel.renameCurrentPage(newName)
                        viewModel.closeRenamePageDialog()
                    },
                    onDelete = {
                        viewModel.deleteCurrentPage()
                        viewModel.closeRenamePageDialog()
                    },
                    onDismiss = { viewModel.closeRenamePageDialog() }
                )
            }

            // Clear Screen Confirmation Dialog
            if (uiState.showClearConfirmDialog) {
                ClearConfirmDialog(
                    pageName = uiState.currentPageName,
                    onConfirm = { viewModel.clearCurrentPage() },
                    onDismiss = { viewModel.closeClearConfirmDialog() }
                )
            }

            // In-App Toast Banner
            AnimatedVisibility(
                visible = uiState.toastMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                uiState.toastMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                            .border(1.dp, PosCardBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = PosAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = PosTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
