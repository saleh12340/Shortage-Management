package com.example.domain

import com.example.data.local.GroceryItemEntity
import com.example.data.local.SplitSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportFormatters {

    fun generateMandatoryPreambleText(
        pageName: String,
        rightItems: List<GroceryItemEntity>,
        leftItems: List<GroceryItemEntity>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        val currentDateTime = dateFormat.format(Date())
        val totalCount = rightItems.size + leftItems.size

        val sb = StringBuilder()
        sb.appendLine("🏪 بقالة العزي للمواد الغذائية")
        sb.appendLine("📅 التاريخ والوقت: $currentDateTime")
        sb.appendLine("📄 $pageName | إجمالي الأصناف: $totalCount")
        sb.appendLine("========================================")
        sb.appendLine("--- 🔷 الشق الأيمن ---")
        if (rightItems.isEmpty()) {
            sb.appendLine("(لا توجد أصناف)")
        } else {
            rightItems.forEach { item ->
                sb.appendLine("${item.qty} | ${item.name}")
            }
        }
        sb.appendLine()
        sb.appendLine("--- 🟢 الشق الأيسر ---")
        if (leftItems.isEmpty()) {
            sb.appendLine("(لا توجد أصناف)")
        } else {
            leftItems.forEach { item ->
                sb.appendLine("${item.qty} | ${item.name}")
            }
        }
        sb.appendLine("========================================")
        return sb.toString()
    }

    fun generateHtmlReport(
        pageName: String,
        rightItems: List<GroceryItemEntity>,
        leftItems: List<GroceryItemEntity>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        val currentDateTime = dateFormat.format(Date())
        val totalCount = rightItems.size + leftItems.size

        val rightRows = rightItems.mapIndexed { idx, item ->
            "<tr><td style='text-align:center;font-weight:bold;color:#1e3a8a;'>${item.qty}</td><td style='text-align:right;'>${item.name}</td><td style='text-align:center;color:#64748b;'>${idx + 1}</td></tr>"
        }.joinToString("")

        val leftRows = leftItems.mapIndexed { idx, item ->
            "<tr><td style='text-align:center;font-weight:bold;color:#065f46;'>${item.qty}</td><td style='text-align:right;'>${item.name}</td><td style='text-align:center;color:#64748b;'>${idx + 1}</td></tr>"
        }.joinToString("")

        return """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="utf-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        margin: 20px;
                        color: #0f172a;
                        background: #ffffff;
                    }
                    .header {
                        text-align: center;
                        border-bottom: 2px solid #0f172a;
                        padding-bottom: 12px;
                        margin-bottom: 20px;
                    }
                    .store-title {
                        font-size: 24px;
                        font-weight: 900;
                        color: #0f172a;
                        margin: 0;
                    }
                    .sub-title {
                        font-size: 16px;
                        color: #2563eb;
                        margin: 4px 0;
                        font-weight: bold;
                    }
                    .meta-info {
                        font-size: 13px;
                        color: #475569;
                        margin-top: 6px;
                    }
                    .grid-container {
                        display: flex;
                        gap: 20px;
                    }
                    .split-col {
                        flex: 1;
                        border: 1px solid #cbd5e1;
                        border-radius: 8px;
                        overflow: hidden;
                    }
                    .col-header-blue {
                        background: #2563eb;
                        color: white;
                        padding: 8px 12px;
                        font-size: 16px;
                        font-weight: bold;
                        display: flex;
                        justify-content: space-between;
                    }
                    .col-header-green {
                        background: #059669;
                        color: white;
                        padding: 8px 12px;
                        font-size: 16px;
                        font-weight: bold;
                        display: flex;
                        justify-content: space-between;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        font-size: 13px;
                    }
                    th {
                        background: #f1f5f9;
                        color: #334155;
                        padding: 8px;
                        font-weight: bold;
                        border-bottom: 1px solid #cbd5e1;
                    }
                    td {
                        padding: 7px 10px;
                        border-bottom: 1px solid #e2e8f0;
                    }
                    tr:nth-child(even) {
                        background: #f8fafc;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        font-size: 12px;
                        color: #64748b;
                        border-top: 1px dashed #cbd5e1;
                        padding-top: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 class="store-title">🏪 بقالة العزي للمواد الغذائية</h1>
                    <div class="sub-title">كشف نواقص المواد الغذائية والطلبيات</div>
                    <div class="meta-info">
                        <strong>التاريخ والوقت:</strong> $currentDateTime &nbsp;|&nbsp;
                        <strong>الصفحة:</strong> $pageName &nbsp;|&nbsp;
                        <strong>إجمالي الأصناف:</strong> $totalCount صنف
                    </div>
                </div>

                <div class="grid-container">
                    <div class="split-col">
                        <div class="col-header-blue">
                            <span>🔷 الشق الأيمن</span>
                            <span>(${rightItems.size} صنف)</span>
                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 20%; text-align:center;">العدد</th>
                                    <th style="width: 65%; text-align:right;">اسم الصنف</th>
                                    <th style="width: 15%; text-align:center;">م</th>
                                </tr>
                            </thead>
                            <tbody>
                                $rightRows
                            </tbody>
                        </table>
                    </div>

                    <div class="split-col">
                        <div class="col-header-green">
                            <span>🟢 الشق الأيسر</span>
                            <span>(${leftItems.size} صنف)</span>
                        </div>
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 20%; text-align:center;">العدد</th>
                                    <th style="width: 65%; text-align:right;">اسم الصنف</th>
                                    <th style="width: 15%; text-align:center;">م</th>
                                </tr>
                            </thead>
                            <tbody>
                                $leftRows
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="footer">
                    تم إنشاء هذا الكشف بواسطة تطبيق ملاحظات نواقص بقالة العزي &bull; $currentDateTime
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
