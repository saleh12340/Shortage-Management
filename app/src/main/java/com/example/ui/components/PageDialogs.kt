package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PosAmber
import com.example.ui.theme.PosCardBorder
import com.example.ui.theme.PosCardDark
import com.example.ui.theme.PosRedActive
import com.example.ui.theme.PosTextMuted
import com.example.ui.theme.PosTextPrimary
import com.example.ui.theme.PosTextSecondary
import com.example.ui.theme.SplitRightBlue

@Composable
fun NewPageDialog(
    initialName: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pageName by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PosCardDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = PosAmber)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة صفحة جديدة", color = PosTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "أدخل اسم الصفحة (مثلاً: طلبيات الغد، نواقص المنظفات...):",
                    color = PosTextSecondary,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = pageName,
                    onValueChange = { pageName = it },
                    placeholder = { Text("اسم الصفحة", color = PosTextMuted) },
                    textStyle = TextStyle(color = PosTextPrimary, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SplitRightBlue,
                        unfocusedBorderColor = PosCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_page_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pageName) },
                colors = ButtonDefaults.buttonColors(containerColor = SplitRightBlue),
                modifier = Modifier.testTag("confirm_create_page_btn")
            ) {
                Text("إنشاء", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء", color = PosTextSecondary)
            }
        }
    )
}

@Composable
fun RenamePageDialog(
    currentPageName: String,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentPageName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PosCardDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = PosAmber)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إدارة الصفحة", color = PosTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "تعديل اسم الصفحة الحالية:", color = PosTextSecondary, fontSize = 13.sp)
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    textStyle = TextStyle(color = PosTextPrimary, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SplitRightBlue,
                        unfocusedBorderColor = PosCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRename(newName) },
                colors = ButtonDefaults.buttonColors(containerColor = SplitRightBlue)
            ) {
                Text("حفظ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { onDelete(); onDismiss() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PosRedActive)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف")
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("إلغاء", color = PosTextSecondary)
                }
            }
        }
    )
}

@Composable
fun ClearConfirmDialog(
    pageName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PosCardDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = PosRedActive)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تأكيد مسح الصفحة", color = PosTextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        },
        text = {
            Text(
                text = "هل أنت متأكد من مسح جميع أصناف الشقين في \"$pageName\"؟ لا يمكن التراجع عن هذا الإجراء.",
                color = PosTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = PosRedActive),
                modifier = Modifier.testTag("confirm_clear_page_btn")
            ) {
                Text("مسح الكل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء", color = PosTextSecondary)
            }
        }
    )
}
