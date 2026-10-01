package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.SplitSection
import com.example.data.local.SuggestionEntity
import com.example.domain.SpeechTargetField
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
fun ExplicitInputBar(
    qty: String,
    name: String,
    targetSection: SplitSection,
    suggestions: List<SuggestionEntity>,
    isListening: Boolean,
    listeningTarget: SpeechTargetField,
    onQtyChange: (String) -> Unit,
    onIncrementQty: () -> Unit,
    onDecrementQty: () -> Unit,
    onNameChange: (String) -> Unit,
    onSuggestionSelect: (String) -> Unit,
    onAddItem: () -> Unit,
    onStartListening: (SpeechTargetField) -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    var qtyTextFieldValue by remember(qty) {
        mutableStateOf(TextFieldValue(text = qty, selection = TextRange(0, qty.length)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PosCardDark, RoundedCornerShape(12.dp))
            .border(1.dp, PosCardBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .testTag("explicit_input_bar")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quantity Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(PosAmberBg, RoundedCornerShape(8.dp))
                        .border(1.dp, PosAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrementQty,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("qty_decrement_btn")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_remove_circle),
                            contentDescription = "إنقاص العدد",
                            tint = PosAmber,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    OutlinedTextField(
                        value = qtyTextFieldValue,
                        onValueChange = { newValue ->
                            val digits = newValue.text.filter { it.isDigit() }
                            qtyTextFieldValue = newValue.copy(text = digits)
                            onQtyChange(digits)
                        },
                        textStyle = TextStyle(
                            color = PosAmberLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = PosAmber
                        ),
                        modifier = Modifier
                            .width(38.dp)
                            .height(38.dp)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    qtyTextFieldValue = qtyTextFieldValue.copy(
                                        selection = TextRange(0, qtyTextFieldValue.text.length)
                                    )
                                }
                            }
                            .testTag("qty_input_field")
                    )

                    IconButton(
                        onClick = onIncrementQty,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("qty_increment_btn")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_circle),
                            contentDescription = "زيادة العدد",
                            tint = PosAmber,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Dedicated Mic for Quantity (Hold to talk)
                    val isListeningQty = isListening && listeningTarget == SpeechTargetField.QUANTITY_ONLY
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isListeningQty) PosRedActive.copy(alpha = 0.25f) else Color.Transparent)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    onStartListening(SpeechTargetField.QUANTITY_ONLY)
                                    waitForUpOrCancellation()
                                    onStopListening()
                                }
                            }
                            .testTag("qty_mic_btn")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic),
                            contentDescription = "مايك العدد",
                            tint = if (isListeningQty) PosRedActive else PosAmber,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Item Name Field with Autocomplete & Dedicated Mic
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = {
                        Text(
                            text = "اكتب الصنف (بر، سكر، رز...)",
                            color = PosTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    textStyle = TextStyle(
                        color = PosTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onAddItem() }
                    ),
                    trailingIcon = {
                        val isListeningName = isListening && listeningTarget == SpeechTargetField.NAME_ONLY
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isListeningName) PosRedActive.copy(alpha = 0.25f) else Color.Transparent)
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        onStartListening(SpeechTargetField.NAME_ONLY)
                                        waitForUpOrCancellation()
                                        onStopListening()
                                    }
                                }
                                .testTag("name_mic_btn")
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mic),
                                contentDescription = "مايك الصنف",
                                tint = if (isListeningName) PosRedActive else SplitRightBlue,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SplitRightBlue,
                        unfocusedBorderColor = PosCardBorder,
                        focusedContainerColor = Color(0xFF030712),
                        unfocusedContainerColor = Color(0xFF030712),
                        cursorColor = SplitRightBlue
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("item_name_input_field")
                )
            }

            // Autocomplete Suggestions Chips Row
            if (suggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "مقترح:",
                        color = PosTextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    suggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                                .clickable { onSuggestionSelect(suggestion.name) }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = suggestion.name,
                                color = PosTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Add Item Button
            val isRight = targetSection == SplitSection.RIGHT
            Button(
                onClick = onAddItem,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRight) SplitRightBlue else SplitLeftGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("add_item_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRight) "إضافة الصنف إلى [الشق الأيمن 🔷]" else "إضافة الصنف إلى [الشق الأيسر 🟢]",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
