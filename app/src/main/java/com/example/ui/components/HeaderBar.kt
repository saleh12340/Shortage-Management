package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PosAmber
import com.example.ui.theme.PosCardBorder
import com.example.ui.theme.PosCardDark
import com.example.ui.theme.PosGreenOnline
import com.example.ui.theme.PosRedActive
import com.example.ui.theme.PosTextMuted
import com.example.ui.theme.PosTextPrimary
import com.example.ui.theme.PosTextSecondary
import com.example.ui.theme.SplitLeftGreen
import com.example.ui.theme.SplitRightBlue

@Composable
fun HeaderBar(
    storeTitle: String = "بقالة العزي للمواد الغذائية",
    pageName: String,
    totalCount: Int,
    onDirectPrint: () -> Unit,
    onShareImage: () -> Unit,
    onCopyText: () -> Unit,
    onSwapSplits: () -> Unit,
    onClearScreen: () -> Unit,
    onOpenReceiptPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PosCardDark)
            .border(width = 1.dp, color = PosCardBorder, shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("compact_header_bar"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Top Row: Store Identity + Count Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .scale(pulseScale)
                            .background(PosGreenOnline.copy(alpha = 0.25f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(PosGreenOnline, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    painter = painterResource(id = R.drawable.ic_storefront),
                    contentDescription = null,
                    tint = PosAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = storeTitle,
                    color = PosTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Page info & count badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF030712))
                    .border(0.8.dp, PosCardBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$pageName • $totalCount صنف",
                    color = PosAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Row: Dedicated Action Buttons for Each Function
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // 1. Direct System / Bluetooth Print Button (خادم طباعة النظام)
            HeaderActionButton(
                label = "طباعة النظام",
                iconRes = R.drawable.ic_print,
                badgeColor = PosAmber,
                onClick = onDirectPrint,
                testTag = "direct_system_print_btn"
            )

            // 2. Share Image Button (مشاركة كصورة للواتساب)
            HeaderActionButton(
                label = "مشاركة صورة",
                iconRes = R.drawable.ic_image,
                badgeColor = SplitLeftGreen,
                onClick = onShareImage,
                testTag = "share_image_btn"
            )

            // 3. Copy Text Button (نسخ القائمة للحافظة)
            HeaderActionButton(
                label = "نسخ النص",
                iconRes = R.drawable.ic_content_copy,
                badgeColor = SplitRightBlue,
                onClick = onCopyText,
                testTag = "copy_text_btn"
            )

            // 4. Swap Splits Button (قلب الشقين)
            HeaderActionButton(
                label = "قلب الشقين",
                iconRes = R.drawable.ic_swap_horiz,
                badgeColor = Color(0xFF818CF8),
                onClick = onSwapSplits,
                testTag = "swap_splits_btn"
            )

            // 5. Thermal 58mm Preview Dialog Button (معاينة الإيصال)
            HeaderActionButton(
                label = "إيصال 58mm",
                iconRes = R.drawable.ic_bluetooth,
                badgeColor = Color(0xFFF97316),
                onClick = onOpenReceiptPreview,
                testTag = "preview_receipt_btn"
            )

            // 6. Clear Screen Button (مسح الكل)
            HeaderActionButton(
                label = "مسح",
                iconRes = R.drawable.ic_delete_sweep,
                badgeColor = PosRedActive,
                onClick = onClearScreen,
                testTag = "clear_screen_btn"
            )
        }
    }
}

@Composable
private fun HeaderActionButton(
    label: String,
    iconRes: Int,
    badgeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(badgeColor.copy(alpha = 0.16f))
            .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(7.dp))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 5.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = badgeColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = PosTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
