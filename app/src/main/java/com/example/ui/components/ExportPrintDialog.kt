package com.example.ui.components

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
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
    var selectedTab by remember { mutableStateOf(1) } // Open on the actual receipt preview.

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
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
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
                        Spacer(Modifier.width(6.dp))
                        Text("الإيصال 58mm", color = PosTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "إغلاق", tint = PosTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("الإجراءات", "معاينة 58mm").forEachIndexed { index, title ->
                        val selected = selectedTab == index
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) SplitRightBlue else Color(0xFF1E293B))
                                .border(1.dp, if (selected) Color(0xFF60A5FA) else PosCardBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedTab = index }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(title, color = if (selected) Color.White else PosTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(340.dp)
                ) {
                    if (selectedTab == 0) {
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompactActionCard(
                                title = "طباعة عبر نظام الهاتف",
                                description = "يفتح معاينة الطباعة الخاصة بالنظام ثم تختار طابعة Bluetooth أو أي طابعة متاحة",
                                iconRes = R.drawable.ic_print,
                                badgeColor = SplitRightBlue,
                                onClick = {
                                    if (activity != null) {
                                        PrintHelper.printThermalReceipt(activity, receiptBitmap, pageName)
                                    } else {
                                        onShowToast("خدمة الطباعة غير متوفرة")
                                    }
                                }
                            )

                            CompactActionCard(
                                title = "حفظ الإيصال كصورة",
                                description = "يحفظ نفس الإيصال الأبيض المضغوط بجودة HD في صور الجهاز",
                                iconRes = R.drawable.ic_image,
                                badgeColor = SplitLeftGreen,
                                onClick = {
                                    val uri = PrintHelper.saveReceiptImage(context, receiptBitmap, pageName)
                                    onShowToast(if (uri != null) "تم حفظ الإيصال كصورة ✓" else "تعذر حفظ الصورة")
                                }
                            )

                            CompactActionCard(
                                title = "مشاركة الإيصال كصورة",
                                description = "مشاركة نفس صورة الإيصال عبر واتساب أو أي تطبيق مشاركة",
                                iconRes = R.drawable.ic_image,
                                badgeColor = PosGreenOnline,
                                onClick = {
                                    PrintHelper.shareReceiptAsImage(context, pageName, rightItems, leftItems) {
                                        onShowToast("تم تجهيز الإيصال للمشاركة ✓")
                                    }
                                }
                            )

                            CompactActionCard(
                                title = "نسخ النص",
                                description = "نسخ كشف النواقص كنص عند الحاجة",
                                iconRes = R.drawable.ic_content_copy,
                                badgeColor = PosAmber,
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("نواقص بقالة العزي", mandatoryText))
                                    onShowToast("تم نسخ النص ✓")
                                }
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "هذه هي الصورة نفسها التي ستُحفظ أو تُرسل أو تُرسل للطباعة",
                                color = PosTextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .width(230.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(4.dp))
                                    .padding(5.dp)
                            ) {
                                Image(
                                    bitmap = receiptBitmap.asImageBitmap(),
                                    contentDescription = "معاينة إيصال بقالة العزي 58mm",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق", color = PosTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    Surface(
        onClick = onClick,
        color = Color(0xFF0B132B),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, PosCardBorder, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = PosTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(description, color = PosTextSecondary, fontSize = 10.sp, lineHeight = 13.sp)
            }
        }
    }
}
