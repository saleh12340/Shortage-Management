package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GroceryItemEntity
import com.example.data.local.SplitSection
import com.example.ui.theme.PosAmber
import com.example.ui.theme.PosAmberLight
import com.example.ui.theme.PosCardBorder
import com.example.ui.theme.PosCardDark
import com.example.ui.theme.PosRedActive
import com.example.ui.theme.PosTextMuted
import com.example.ui.theme.PosTextPrimary
import com.example.ui.theme.PosTextSecondary
import com.example.ui.theme.SplitLeftBorder
import com.example.ui.theme.SplitLeftGreen
import com.example.ui.theme.SplitLeftGreenDark
import com.example.ui.theme.SplitLeftGreenLight
import com.example.ui.theme.SplitRightBlue
import com.example.ui.theme.SplitRightBlueDark
import com.example.ui.theme.SplitRightBlueLight
import com.example.ui.theme.SplitRightBorder

@Composable
fun DualSplitGrid(
    rightItems: List<GroceryItemEntity>,
    leftItems: List<GroceryItemEntity>,
    fontSizeSp: Float,
    onItemClick: (GroceryItemEntity) -> Unit,
    onMoveItem: (GroceryItemEntity) -> Unit,
    onDeleteItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Right Split Panel (50% Width) - Royal Blue Theme
        SplitPanelColumn(
            title = "🔷 الشق الأيمن",
            count = rightItems.size,
            items = rightItems,
            section = SplitSection.RIGHT,
            headerGradient = listOf(SplitRightBlueDark, SplitRightBlue),
            borderColor = SplitRightBorder,
            qtyColor = PosAmber,
            qtyTextColor = PosAmberLight,
            fontSizeSp = fontSizeSp,
            onItemClick = onItemClick,
            onMoveItem = onMoveItem,
            onDeleteItem = onDeleteItem,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("right_split_panel")
        )

        // Left Split Panel (50% Width) - Emerald Green Theme
        SplitPanelColumn(
            title = "🟢 الشق الأيسر",
            count = leftItems.size,
            items = leftItems,
            section = SplitSection.LEFT,
            headerGradient = listOf(SplitLeftGreenDark, SplitLeftGreen),
            borderColor = SplitLeftBorder,
            qtyColor = SplitLeftGreen,
            qtyTextColor = Color(0xFFA7F3D0),
            fontSizeSp = fontSizeSp,
            onItemClick = onItemClick,
            onMoveItem = onMoveItem,
            onDeleteItem = onDeleteItem,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("left_split_panel")
        )
    }
}

@Composable
private fun SplitPanelColumn(
    title: String,
    count: Int,
    items: List<GroceryItemEntity>,
    section: SplitSection,
    headerGradient: List<Color>,
    borderColor: Color,
    qtyColor: Color,
    qtyTextColor: Color,
    fontSizeSp: Float,
    onItemClick: (GroceryItemEntity) -> Unit,
    onMoveItem: (GroceryItemEntity) -> Unit,
    onDeleteItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(PosCardDark, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Column Header with Gradient and Item Count Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(headerGradient))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )

                // Item Count Pill
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$count صنف",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Table Column Sub-Headers: [العدد] | [اسم الصنف]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030712))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "العدد",
                color = PosTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "اسم الصنف",
                color = PosTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "إجراء",
                color = PosTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
        }

        // Items List or Empty State
        if (items.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "لا توجد نواقص",
                        color = PosTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "أضف صوتياً أو يدوياً",
                        color = Color(0xFF475569),
                        fontSize = 10.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    val isEven = index % 2 == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isEven) Color(0xFF0B132B).copy(alpha = 0.6f) else Color(0xFF0F172A))
                            .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .clickable { onItemClick(item) }
                            .padding(horizontal = 4.dp, vertical = 5.dp)
                            .testTag("item_row_${item.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Editable Quantity Cell Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(32.dp)
                                .background(qtyColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, qtyColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "${item.qty}",
                                color = qtyTextColor,
                                fontSize = fontSizeSp.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Item Name Cell (Clickable for in-place edit)
                        Text(
                            text = item.name,
                            color = PosTextPrimary,
                            fontSize = fontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Action Buttons: Transfer to other split (Swap) & Delete
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Move to opposite split button
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E293B))
                                    .clickable { onMoveItem(item) }
                                    .testTag("move_item_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "نقل للشق الآخر",
                                    tint = PosTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Delete button
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PosRedActive.copy(alpha = 0.15f))
                                    .clickable { onDeleteItem(item.id) }
                                    .testTag("delete_item_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف الصنف",
                                    tint = PosRedActive,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
