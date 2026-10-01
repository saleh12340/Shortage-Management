package com.example.domain

import com.example.data.local.SplitSection

sealed class VoiceParseResult {
    data class AddItem(val name: String, val qty: Int, val targetSection: SplitSection?) : VoiceParseResult()
    data class Command(val type: CommandType) : VoiceParseResult()
    data class QuantityOnly(val qty: Int) : VoiceParseResult()
    data class NameOnly(val name: String) : VoiceParseResult()
    object Unrecognized : VoiceParseResult()
}

enum class CommandType {
    PRINT,
    COPY,
    SWAP,
    CLEAR,
    NEW_PAGE
}

object VoiceParser {

    private val numberMap = mapOf(
        "واحد" to 1, "واحدة" to 1, "حبه" to 1, "حبة" to 1, "كيس" to 1, "كرتون" to 1, "علبة" to 1, "علبه" to 1,
        "اثنين" to 2, "إثنين" to 2, "اثنان" to 2, "إثنان" to 2, "حبتين" to 2, "كيسين" to 2, "كرتونين" to 2, "علبتين" to 2, "باكتين" to 2,
        "ثلاثة" to 3, "ثلاثه" to 3, "تلاتة" to 3, "تلاته" to 3, "ثلاث" to 3, "تلات" to 3,
        "أربعة" to 4, "اربعة" to 4, "أربعه" to 4, "اربعه" to 4, "اربع" to 4, "أربع" to 4,
        "خمسة" to 5, "خمسه" to 5, "خمس" to 5,
        "ستة" to 6, "سته" to 6, "ست" to 6,
        "سبعة" to 7, "سبعه" to 7, "سبع" to 7,
        "ثمانية" to 8, "ثمانيه" to 8, "تمانية" to 8, "تمانيه" to 8, "ثمان" to 8, "تمان" to 8,
        "تسعة" to 9, "تسعه" to 9, "تسع" to 9,
        "عشرة" to 10, "عشره" to 10, "عشر" to 10,
        "أحد عشر" to 11, "احد عشر" to 11, "حداش" to 11, "إحدى عشر" to 11,
        "اثنا عشر" to 12, "إثنا عشر" to 12, "اثناعش" to 12, "درزن" to 12, "دستة" to 12,
        "ثلاثة عشر" to 13, "ثلاثه عشر" to 13, "اربعة عشر" to 14, "أربعة عشر" to 14, "اربعه عشر" to 14, "خمسة عشر" to 15, "خمسه عشر" to 15,
        "ستة عشر" to 16, "سته عشر" to 16, "سبعة عشر" to 17, "سبعه عشر" to 17, "ثمانية عشر" to 18, "ثمانيه عشر" to 18, "تسعة عشر" to 19, "تسعه عشر" to 19,
        "عشرون" to 20, "عشرين" to 20, "درزنين" to 24, "دستتين" to 24,
        "ثلاثون" to 30, "ثلاثين" to 30, "ثلاثة درازن" to 36,
        "أربعون" to 40, "اربعين" to 40, "اربعون" to 40,
        "خمسون" to 50, "خمسين" to 50,
        "ستون" to 60, "ستين" to 60,
        "سبعون" to 70, "سبعين" to 70,
        "ثمانون" to 80, "ثمانين" to 80,
        "تسعون" to 90, "تسعين" to 90,
        "مئة" to 100, "مائة" to 100, "ميه" to 100, "مية" to 100,
        "مئتان" to 200, "مئتين" to 200, "ميتين" to 200,
        "خمسمائة" to 500, "خمسمية" to 500,
        "ألف" to 1000, "الف" to 1000
    )

    private val leftKeywords = listOf("أيسر", "ايسر", "يسار", "اليسار", "شمال", "الشمال")
    private val rightKeywords = listOf("أيمن", "ايمن", "يمين", "اليمين")

    fun parseSpokenSentence(rawText: String): VoiceParseResult {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return VoiceParseResult.Unrecognized

        // 1. Check Voice Commands
        val normalizedLower = trimmed.lowercase()
        when {
            normalizedLower == "طباعة" || normalizedLower == "اطبع" || normalizedLower.contains("شاشة الطباعة") ->
                return VoiceParseResult.Command(CommandType.PRINT)

            normalizedLower == "نسخ" || normalizedLower == "انسخ" || normalizedLower.contains("نسخ القائمة") ->
                return VoiceParseResult.Command(CommandType.COPY)

            normalizedLower == "قلب" || normalizedLower.contains("قلب الشقين") || normalizedLower.contains("تبديل الشقين") ->
                return VoiceParseResult.Command(CommandType.SWAP)

            normalizedLower == "مسح" || normalizedLower.contains("مسح الشاشة") || normalizedLower.contains("مسح الكل") ->
                return VoiceParseResult.Command(CommandType.CLEAR)

            normalizedLower == "صفحة جديدة" || normalizedLower.contains("اضافة صفحة") || normalizedLower.contains("صفحه جديده") ->
                return VoiceParseResult.Command(CommandType.NEW_PAGE)
        }

        // 2. Detect Routing Target
        var detectedTarget: SplitSection? = null
        val words = trimmed.split("\\s+".toRegex()).toMutableList()

        for (kw in leftKeywords) {
            if (words.any { it.equals(kw, ignoreCase = true) }) {
                detectedTarget = SplitSection.LEFT
                words.removeAll { it.equals(kw, ignoreCase = true) }
                break
            }
        }
        if (detectedTarget == null) {
            for (kw in rightKeywords) {
                if (words.any { it.equals(kw, ignoreCase = true) }) {
                    detectedTarget = SplitSection.RIGHT
                    words.removeAll { it.equals(kw, ignoreCase = true) }
                    break
                }
            }
        }

        // Remove filler prefixes like "سجل", "حط", "اكتب", "ضيف", "في"
        val ignoredPrefixes = listOf("سجل", "حط", "اكتب", "ضيف", "أضف", "في", "على", "بالشق", "في الشق")
        if (words.isNotEmpty() && ignoredPrefixes.contains(words.first())) {
            words.removeAt(0)
        }

        if (words.isEmpty()) {
            return VoiceParseResult.Unrecognized
        }

        // 3. Extract Quantity
        var extractedQty = 1
        var qtyFound = false

        // Check if first word is a digit (Arabic/English: "5", "١٢")
        val digitMatch = Regex("^([0-9]+|[٠-٩]+)$").find(words.first())
        if (digitMatch != null) {
            extractedQty = convertNumeralsToInt(digitMatch.value)
            words.removeAt(0)
            qtyFound = true
        } else {
            // Check composite number words like "خمسة عشر" or "ثلاثين" or "خمسة"
            if (words.size >= 2) {
                val twoWordCandidate = "${words[0]} ${words[1]}"
                if (numberMap.containsKey(twoWordCandidate)) {
                    extractedQty = numberMap[twoWordCandidate] ?: 1
                    words.removeAt(0)
                    words.removeAt(0)
                    qtyFound = true
                }
            }
            if (!qtyFound && words.isNotEmpty()) {
                val firstWord = words.first()
                if (numberMap.containsKey(firstWord)) {
                    extractedQty = numberMap[firstWord] ?: 1
                    words.removeAt(0)
                    qtyFound = true
                }
            }
        }

        // Keep natural item wording such as "أكياس بر" in the item-name field.
        // Only remove quantity-packaging words that are themselves the quantity unit
        // (e.g. "درزن حليب" => "حليب"). This preserves the user's spoken item text.
        val quantityUnitWords = setOf("درزن", "دستة", "دسته")
        while (words.isNotEmpty() && quantityUnitWords.contains(words.first().trim())) {
            words.removeAt(0)
        }
        val remainingName = words.joinToString(" ").trim()
        if (remainingName.isEmpty()) {
            if (qtyFound) {
                return VoiceParseResult.QuantityOnly(extractedQty)
            }
            return VoiceParseResult.Unrecognized
        }

        return VoiceParseResult.AddItem(
            name = remainingName,
            qty = extractedQty.coerceAtLeast(1),
            targetSection = detectedTarget
        )
    }

    fun parseQuantityOnly(rawText: String): Int? {
        val trimmed = rawText.trim()
        val digit = convertNumeralsToInt(trimmed)
        if (digit > 0) return digit
        // Look up in number map
        for ((word, num) in numberMap) {
            if (trimmed.contains(word)) return num
        }
        return null
    }

    private fun convertNumeralsToInt(str: String): Int {
        val arabicToEng = mapOf(
            '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
            '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9'
        )
        val normalized = str.trim().replace("،", "").replace(",", "").replace(" ", "")
        val converted = normalized.map { arabicToEng[it] ?: it }.joinToString("")
        val digitsOnly = converted.filter { it.isDigit() }
        return digitsOnly.toIntOrNull() ?: 0
    }
}
