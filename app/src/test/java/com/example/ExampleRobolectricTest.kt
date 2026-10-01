package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GroceryItemEntity
import com.example.data.local.SplitSection
import com.example.domain.CommandType
import com.example.domain.ExportFormatters
import com.example.domain.VoiceParseResult
import com.example.domain.VoiceParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("نواقص العزي", appName)
    }

    @Test
    fun `test arabic voice parsing for quantity and item name`() {
        val result = VoiceParser.parseSpokenSentence("خمسة أكياس بر")
        assertTrue(result is VoiceParseResult.AddItem)
        val item = result as VoiceParseResult.AddItem
        assertEquals(5, item.qty)
        assertEquals("أكياس بر", item.name)
    }

    @Test
    fun `test arabic voice parsing with target routing left`() {
        val result = VoiceParser.parseSpokenSentence("يسار درزن حليب")
        assertTrue(result is VoiceParseResult.AddItem)
        val item = result as VoiceParseResult.AddItem
        assertEquals(12, item.qty)
        assertEquals("حليب", item.name)
        assertEquals(SplitSection.LEFT, item.targetSection)
    }

    @Test
    fun `test voice command detection`() {
        val resultPrint = VoiceParser.parseSpokenSentence("طباعة")
        assertTrue(resultPrint is VoiceParseResult.Command)
        assertEquals(CommandType.PRINT, (resultPrint as VoiceParseResult.Command).type)

        val resultSwap = VoiceParser.parseSpokenSentence("قلب الشقين")
        assertTrue(resultSwap is VoiceParseResult.Command)
        assertEquals(CommandType.SWAP, (resultSwap as VoiceParseResult.Command).type)
    }

    @Test
    fun `test mandatory preamble formatting`() {
        val rightItems = listOf(GroceryItemEntity(pageId = "1", section = SplitSection.RIGHT, qty = 5, name = "سكر"))
        val leftItems = listOf(GroceryItemEntity(pageId = "1", section = SplitSection.LEFT, qty = 2, name = "حليب"))
        val formatted = ExportFormatters.generateMandatoryPreambleText("صفحة 1", rightItems, leftItems)

        assertTrue(formatted.contains("🏪 بقالة العزي للمواد الغذائية"))
        assertTrue(formatted.contains("--- 🔷 الشق الأيمن ---"))
        assertTrue(formatted.contains("--- 🟢 الشق الأيسر ---"))
        assertTrue(formatted.contains("5 | سكر"))
        assertTrue(formatted.contains("2 | حليب"))
    }
}
