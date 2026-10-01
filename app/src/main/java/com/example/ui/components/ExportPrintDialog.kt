package com.example.ui.components

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.GroceryItemEntity
import com.example.domain.EscPosGenerator
import com.example.domain.ExportFormatters
import com.example.domain.PrintHelper
import com.example.ui.theme.PosAmber
import com.example.ui.theme.PosCardBorder
import com.example.ui.theme.PosCardDark
import com.example.ui.theme.PosGreenOnline
import com.example.ui.theme.PosTextMuted
import com.example.ui.theme.PosTextPrimary
import com.example.ui.theme.PosTextSecondary
import com.example.ui.theme.SplitLeftGreen
import com.example.ui.theme.SplitRightBlue

@Composable
fun ExportPrintDialog(
    pageName: String,
    rightItems: List<GroceryItemEntity>,
    leftItems: List<GroceryItemEntity>,
    onDismiss: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val mandatoryText = remember(pageName, rightItems, leftItems) {
        ExportFormatters.generateMandatoryPreambleText(pageName, rightItems, leftItems)
    }

    val receiptBitmap: Bitmap = remember(pageName, rightItems, leftItems) {
        EscPosGenerator.create58mmReceiptBitmap(pageName, rightItems, leftItems)
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: خيارات المخرجات, 1: معاينة إيصال 58mm, 2: معاينة النص

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, PosCardBorder, RoundedCornerShape(16.dp)),
            color = PosCardDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = PosAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الطباعة وتصدير النواقص",
                            color = PosTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = PosTextSecondary
                        )
                    }
                }

                // Sub-tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf("خيارات التصدير", "معاينة 58mm", "النص الرسمي")
                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTab == index
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SplitRightBlue else Color(0xFF1E293B))
                                .border(1.dp, if (isSelected) Color(0xFF60A5FA) else PosCardBorder, RoundedCornerShape(8.dp))
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = tabTitle,
                                color = if (isSelected) Color.White else PosTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    }
                }

                // Content based on tab
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(380.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Export Actions List
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 1. System Document Print / PDF (A4)
                                ExportActionCard(
                                    title = "طباعة المستند القياسي وتصدير PDF (A4)",
                                    description = "طباعة كشف الشقين عالي التباين عبر موجه الطباعة أو الحفظ كـ PDF",
                                    icon = Icons.Default.PictureAsPdf,
                                    badgeColor = SplitRightBlue,
                                    onClick = {
                                        if (activity != null) {
                                            val html = ExportFormatters.generateHtmlReport(pageName, rightItems, leftItems)
                                            PrintHelper.printDocument(activity, html)
                                            onShowToast("تم فتح أمر طباعة المستند")
                                        } else {
                                            onShowToast("تعذر الوصول لخدمة الطباعة")
                                        }
                                    }
                                )

                                // 2. Thermal 58mm Bluetooth Printing
                                ExportActionCard(
                                    title = "طباعة إيصال حراري بلوتوث (58mm)",
                                    description = "توليد أوامر ESC/POS المباشرة بتقنية Raster عالي التباين للغة العربية",
                                    icon = Icons.Default.Bluetooth,
                                    badgeColor = PosAmber,
                                    onClick = {
                                        val bytes = EscPosGenerator.bitmapToEscPosBytes(receiptBitmap)
                                        // Copy ESC/POS preview / info
                                        onShowToast("تم توليد أمر ESC/POS (حجم البيانات: ${bytes.size} بايت)")
                                        selectedTab = 1
                                    }
                                )

                                // 3. Share as High-Res Image (WhatsApp / Apps)
                                ExportActionCard(
                                    title = "مشاركة كشف النواقص كـ صورة عالية الدقة",
                                    description = "توليد صورة فخمة لكشف الشقين مع الترويسة ومشاركتها عبر الواتساب",
                                    icon = Icons.Default.Image,
                                    badgeColor = SplitLeftGreen,
                                    onClick = {
                                        PrintHelper.shareDualColumnAsImage(context, pageName, rightItems, leftItems)
                                        onShowToast("جارِ تجهيز ومشاركة صورة الكشف...")
                                    }
                                )

                                // 4. Copy Mandatory Text with Preamble
                                ExportActionCard(
                                    title = "نسخ القائمة كـ نص رسمي للحافظة",
                                    description = "نسخ الترويسة الإلزامية وقائمة الأصناف للمشاركة السريعة",
                                    icon = Icons.Default.ContentCopy,
                                    badgeColor = Color(0xFF8B5CF6),
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("نواقص بقالة العزي", mandatoryText)
                                        clipboard.setPrimaryClip(clip)
                                        onShowToast("تم نسخ القائمة مع الترويسة الرسمية بنجاح 📋")
                                    }
                                )

                                // 5. Direct Text Share (WhatsApp, Messages)
                                ExportActionCard(
                                    title = "إرسال نصي مباشر (واتساب / رسائل)",
                                    description = "فتح قائمة المشاركة لإرسال النص الرسمي مباشرة",
                                    icon = Icons.Default.Share,
                                    badgeColor = PosGreenOnline,
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "كشف نواقص بقالة العزي - $pageName")
                                            putExtra(Intent.EXTRA_TEXT, mandatoryText)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة كشف النواقص"))
                                    }
                                )
                            }
                        }

                        1 -> {
                            // 58mm Thermal Receipt Preview
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "معاينة إيصال 58 ملم عالي التباين (Raster ESC/POS)",
                                    color = PosTextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(4.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Image(
                                        bitmap = receiptBitmap.asImageBitmap(),
                                        contentDescription = "معاينة الإيصال الحراري",
                                        modifier = Modifier.width(280.dp)
                                    )
                                }
                            }
                        }

                        2 -> {
                            // Raw Mandatory Text Preview
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                                        .border(1.dp, PosCardBorder, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = mandatoryText,
                                        color = PosTextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("نواقص بقالة العزي", mandatoryText)
                            clipboard.setPrimaryClip(clip)
                            onShowToast("تم نسخ القائمة للحافظة ✅")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SplitRightBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ النص", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إغلاق", color = PosTextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0B132B),
            contentColor = PosTextPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PosCardBorder, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = PosTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = PosTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
