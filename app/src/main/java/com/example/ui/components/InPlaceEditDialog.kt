package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.GroceryItemEntity
import com.example.data.local.SplitSection
import com.example.ui.theme.PosAmber
import com.example.ui.theme.PosAmberBg
import com.example.ui.theme.PosAmberLight
import com.example.ui.theme.PosCardBorder
import com.example.ui.theme.PosCardDark
import com.example.ui.theme.PosRedActive
import com.example.ui.theme.PosTextMuted
import com.example.ui.theme.PosTextPrimary
import com.example.ui.theme.PosTextSecondary
import com.example.ui.theme.SplitLeftGreen
import com.example.ui.theme.SplitRightBlue

@Composable
fun InPlaceEditDialog(
    item: GroceryItemEntity,
    onSave: (GroceryItemEntity) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var editedName by remember(item.id) { mutableStateOf(item.name) }
    var currentSection by remember(item.id) { mutableStateOf(item.section) }

    var qtyTextFieldValue by remember(item.id) {
        val qtyStr = item.qty.toString()
        mutableStateOf(TextFieldValue(text = qtyStr, selection = TextRange(0, qtyStr.length)))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PosCardDark,
        shape = RoundedCornerShape(14.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = PosAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تعديل الصنف",
                    color = PosTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item Name
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("اسم الصنف", color = PosTextMuted, fontSize = 12.sp) },
                    textStyle = TextStyle(
                        color = PosTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SplitRightBlue,
                        unfocusedBorderColor = PosCardBorder,
                        focusedContainerColor = Color(0xFF030712),
                        unfocusedContainerColor = Color(0xFF030712)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_item_name_input")
                )

                // Quantity with Stepper and Select-All
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PosAmberBg, RoundedCornerShape(8.dp))
                        .border(1.dp, PosAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "العدد / الكمية:",
                        color = PosAmberLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val current = qtyTextFieldValue.text.toIntOrNull() ?: 1
                                val next = (current - 1).coerceAtLeast(1)
                                qtyTextFieldValue = TextFieldValue(text = next.toString(), selection = TextRange(0, next.toString().length))
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_remove_circle),
                                contentDescription = "إنقاص",
                                tint = PosAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        OutlinedTextField(
                            value = qtyTextFieldValue,
                            onValueChange = { newValue ->
                                val digits = newValue.text.filter { it.isDigit() }
                                qtyTextFieldValue = newValue.copy(text = digits)
                            },
                            textStyle = TextStyle(
                                color = PosAmberLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .width(50.dp)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        qtyTextFieldValue = qtyTextFieldValue.copy(
                                            selection = TextRange(0, qtyTextFieldValue.text.length)
                                        )
                                    }
                                }
                                .testTag("edit_qty_input")
                        )

                        IconButton(
                            onClick = {
                                val current = qtyTextFieldValue.text.toIntOrNull() ?: 1
                                val next = current + 1
                                qtyTextFieldValue = TextFieldValue(text = next.toString(), selection = TextRange(0, next.toString().length))
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add_circle),
                                contentDescription = "زيادة",
                                tint = PosAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Section Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isRight = currentSection == SplitSection.RIGHT
                    Button(
                        onClick = { currentSection = SplitSection.RIGHT },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRight) SplitRightBlue else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "أيمن 🔷",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    val isLeft = currentSection == SplitSection.LEFT
                    Button(
                        onClick = { currentSection = SplitSection.LEFT },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLeft) SplitLeftGreen else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "أيسر 🟢",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalQty = qtyTextFieldValue.text.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    val finalName = editedName.trim()
                    if (finalName.isNotEmpty()) {
                        onSave(item.copy(name = finalName, qty = finalQty, section = currentSection))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SplitRightBlue),
                modifier = Modifier.testTag("save_edit_item_btn")
            ) {
                Text("حفظ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { onDelete(item.id); onDismiss() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PosRedActive),
                    modifier = Modifier.testTag("delete_item_from_dialog_btn")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("حذف")
                }

                OutlinedButton(onClick = onDismiss) {
                    Text("إلغاء", color = PosTextSecondary)
                }
            }
        }
    )
}
