package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.SplitRightBlue

@Composable
fun HeaderBar(
    storeTitle: String = "بقالة العزي للمواد الغذائية",
    pageName: String,
    totalCount: Int,
    onSwapSplits: () -> Unit,
    onClearScreen: () -> Unit,
    onOpenPrintDialog: () -> Unit,
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PosCardDark)
            .border(width = 1.dp, color = PosCardBorder, shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("compact_header_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Store Title & Pulse Dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .scale(pulseScale)
                            .background(PosGreenOnline.copy(alpha = 0.25f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(PosGreenOnline, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_storefront),
                            contentDescription = null,
                            tint = PosAmber,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = storeTitle,
                            color = PosTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "$pageName • $totalCount صنف نواقص",
                        color = PosTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick Actions: Swap Splits, Clear Screen, Print
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Swap Splits Button
                FilledTonalIconButton(
                    onClick = onSwapSplits,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = SplitRightBlue.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("swap_splits_button")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_swap_horiz),
                        contentDescription = "قلب الشقين",
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Clear Screen Button
                FilledTonalIconButton(
                    onClick = onClearScreen,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = PosRedActive.copy(alpha = 0.2f),
                        contentColor = PosRedActive
                    ),
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("clear_screen_button")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete_sweep),
                        contentDescription = "مسح الشاشة",
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Print & Export Button
                FilledTonalIconButton(
                    onClick = onOpenPrintDialog,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = PosAmber.copy(alpha = 0.2f),
                        contentColor = PosAmber
                    ),
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("print_export_button")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_print),
                        contentDescription = "الطباعة والإخراج",
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
