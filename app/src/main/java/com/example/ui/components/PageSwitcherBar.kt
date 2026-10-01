package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.GroceryPageEntity
import com.example.ui.theme.PosAmber
import com.example.ui.theme.PosCardBorder
import com.example.ui.theme.PosCardDark
import com.example.ui.theme.PosTextPrimary
import com.example.ui.theme.PosTextSecondary
import com.example.ui.theme.SplitRightBlue

@Composable
fun PageSwitcherBar(
    pages: List<GroceryPageEntity>,
    currentPageId: String?,
    fontSizeSp: Float,
    onSelectPage: (String) -> Unit,
    onOpenNewPageDialog: () -> Unit,
    onOpenRenamePageDialog: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onCopyList: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PosCardDark, RoundedCornerShape(10.dp))
            .border(1.dp, PosCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("page_switcher_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Page Tabs
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                pages.forEach { page ->
                    val isSelected = page.id == currentPageId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) SplitRightBlue else Color(0xFF1E293B)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                if (isSelected) onOpenRenamePageDialog() else onSelectPage(page.id)
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("page_tab_${page.id}")
                    ) {
                        Text(
                            text = page.pageName,
                            color = if (isSelected) Color.White else PosTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        )
                    }
                }

                // Add Page Button (+)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .clickable { onOpenNewPageDialog() }
                        .testTag("add_page_tab_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "صفحة جديدة",
                        tint = PosAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Font Size Controls (- / [12] / +)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF030712), RoundedCornerShape(6.dp))
                    .padding(horizontal = 3.dp, vertical = 2.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onDecreaseFontSize() }
                ) {
                    Text(
                        text = "A-",
                        color = PosTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${fontSizeSp.toInt()}",
                    color = PosAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 3.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onIncreaseFontSize() }
                ) {
                    Text(
                        text = "A+",
                        color = PosTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Copy List as Text Button
            FilledTonalIconButton(
                onClick = onCopyList,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = SplitRightBlue.copy(alpha = 0.25f),
                    contentColor = Color(0xFF93C5FD)
                ),
                modifier = Modifier
                    .size(28.dp)
                    .testTag("quick_copy_list_btn")
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_content_copy),
                    contentDescription = "نسخ القائمة",
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
