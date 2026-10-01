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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
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

    var selectedTab by remember { mutableStateOf(0) } // 0: الخيارات, 1: معاينة 58mm, 2: النص

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(14.dp))
                .border(1.2.dp, PosCardBorder, RoundedCornerShape(14.dp)),
            color = PosCardDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header Bar (Compact)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_print),
                            contentDescription = null,
                            tint = PosAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الطباعة والتصدير",
                            color = PosTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = PosTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Sub-tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("خيارات الإخراج", "معاينة 58mm", "النص الرسمي")
                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTab == index
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) SplitRightBlue else Color(0xFF1E293B))
                                .border(1.dp, if (isSelected) Color(0xFF60A5FA) else PosCardBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedTab = index }
                                .padding(vertical = 5.dp)
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

                // Content Box (Compressed height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Export Actions List
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. System Thermal Print (58mm Roll)
                                CompactActionCard(
                                    title = "طباعة الإيصال الحراري 58mm (طابعة النظام والبلوتوث)",
                                    description = "إرسال الإيصال المضغوط مباشرة إلى خادم الطباعة بمقاس رول 58mm",
                                    iconRes = R.drawable.ic_print,
                                    badgeColor = SplitRightBlue,
                                    onClick = {
                                        if (activity != null) {
                                            PrintHelper.printThermalReceipt(activity, receiptBitmap, pageName)
                                            onShowToast("تم فتح موجه طباعة الإيصال الحراري 58mm")
                                        } else {
                                            onShowToast("خدمة الطباعة غير متوفرة")
                                        }
                                    }
                                )

                                // 2. Thermal 58mm Bluetooth Printing
                                CompactActionCard(
                                    title = "طباعة إيصال حراري بلوتوث (58mm)",
                                    description = "إيصال مضغوط عالي التباين بدون عناوين زائدة موفر للورق",
                                    iconRes = R.drawable.ic_bluetooth,
                                    badgeColor = PosAmber,
                                    onClick = {
                                        val bytes = EscPosGenerator.bitmapToEscPosBytes(receiptBitmap)
                                        onShowToast("تم تجهيز أمر ESC/POS المضغوط (${bytes.size} بايت)")
                                        selectedTab = 1
                                    }
                                )

                                // 3. Share as High-Res Image (WhatsApp / Apps)
                                CompactActionCard(
                                    title = "مشاركة كشف النواقص كـ صورة",
                                    description = "صورة أنيقة لكشف الشقين للمشاركة السريعة عبر الواتساب",
                                    iconRes = R.drawable.ic_image,
                                    badgeColor = SplitLeftGreen,
                                    onClick = {
                                        PrintHelper.shareDualColumnAsImage(context, pageName, rightItems, leftItems)
                                        onShowToast("جارِ تجهيز ومشاركة الصورة...")
                                    }
                                )

                                // 4. Copy Mandatory Text with Preamble
                                CompactActionCard(
                                    title = "نسخ القائمة كـ نص رسمي للحافظة",
                                    description = "نسخ الترويسة الرسمية وقائمة النواقص بدقة للحافظة",
                                    iconRes = R.drawable.ic_content_copy,
                                    badgeColor = Color(0xFF8B5CF6),
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("نواقص بقالة العزي", mandatoryText)
                                        clipboard.setPrimaryClip(clip)
                                        onShowToast("تم نسخ القائمة مع الترويسة بنجاح 📋")
                                    }
                                )

                                // 5. Direct Text Share (WhatsApp, Messages)
                                CompactActionCard(
                                    title = "إرسال نصي مباشر (واتساب / تطبيقات)",
                                    description = "مشاركة النص المعتمد مع التطبيقات مباشرة",
                                    iconRes = R.drawable.ic_content_copy,
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
                            // Ultra-compact 58mm Thermal Receipt Preview
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "معاينة إيصال 58 ملم (مضغوط وعالي التباين)",
                                    color = PosTextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                ) {
                                    Image(
                                        bitmap = receiptBitmap.asImageBitmap(),
                                        contentDescription = "معاينة الإيصال الحراري المضغوط",
                                        modifier = Modifier.fillMaxWidth()
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
                                        .background(Color(0xFF030712), RoundedCornerShape(6.dp))
                                        .border(1.dp, PosCardBorder, RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = mandatoryText,
                                        color = PosTextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                        Icon(painter = painterResource(id = R.drawable.ic_content_copy), contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ النص", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إغلاق", color = PosTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactActionCard(
    title: String,
    description: String,
    iconRes: Int,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0B132B),
            contentColor = PosTextPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PosCardBorder, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = PosTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = PosTextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
