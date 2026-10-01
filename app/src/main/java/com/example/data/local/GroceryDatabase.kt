package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class GroceryConverters {
    @TypeConverter
    fun fromSplitSection(value: SplitSection): String = value.name

    @TypeConverter
    fun toSplitSection(value: String): SplitSection = try {
        SplitSection.valueOf(value)
    } catch (_: Exception) {
        SplitSection.RIGHT
    }
}

@Database(
    entities = [
        GroceryPageEntity::class,
        GroceryItemEntity::class,
        SuggestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(GroceryConverters::class)
abstract class GroceryDatabase : RoomDatabase() {
    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: GroceryDatabase? = null

        fun getInstance(context: Context): GroceryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GroceryDatabase::class.java,
                    "alezzigrocery_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial seed data on background thread
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).groceryDao()
                                seedInitialData(dao)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(dao: GroceryDao) {
            val initialPageId = UUID.randomUUID().toString()
            val initialPage = GroceryPageEntity(
                id = initialPageId,
                pageName = "صفحة 1",
                orderIndex = 0
            )
            dao.insertPage(initialPage)

            // Initial Shortages Data for right and left sections to make the app immediately useful
            val initialItems = listOf(
                // Right Section (أزرق - سلع أساسية ومواد تموينية)
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.RIGHT, qty = 5, name = "أكياس بر دبي رقم 1", orderIndex = 0),
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.RIGHT, qty = 3, name = "سكر الشفاء 10 كجم", orderIndex = 1),
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.RIGHT, qty = 2, name = "كرتون زيت نباتي حنان", orderIndex = 2),
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.RIGHT, qty = 4, name = "أرز بسمتي الشعلان 5 كجم", orderIndex = 3),
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.RIGHT, qty = 12, name = "شاي الكبوس أحمر خرز", orderIndex = 4),

                // Left Section (أخضر - ألبان، معلبات، ومنظفات)
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.LEFT, qty = 6, name = "طبق بيض بلدي طازج", orderIndex = 0),
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.LEFT, qty = 24, name = "حليب وادي فاطمة مبخر", orderIndex = 1),
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.LEFT, qty = 10, name = "صلصة طماطم السعودية", orderIndex = 2),
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.LEFT, qty = 2, name = "كرتون صابون بونكس أزرق", orderIndex = 3),
                GroceryItemEntity(pageId = initialPageId, section = SplitSection.LEFT, qty = 8, name = "جبن مثلثات لافاش كيري", orderIndex = 4)
            )
            dao.insertItems(initialItems)

            // Top Grocery Staples Autocomplete Seed
            val popularStaples = listOf(
                "أكياس بر", "دقيق أبيض ممتاز", "سكر 10 كجم", "سكر 5 كجم",
                "أرز الشعلان", "أرز الوليمة", "زيت نباتي عافية", "زيت نور",
                "سمن القمرية", "شاي كبوس", "شاي ربيع", "حليب وادي فاطمة",
                "حليب نيدو مجفف", "جبن كرافت سائل", "جبن مثلثات", "طبق بيض",
                "تونة هنا خفيف", "تونة ريم", "سردين مغربي", "صلصة السعودية",
                "مكرونة قودي", "شعيرية الوفاء", "ملح يودي ناعم", "خميرة فورية",
                "صابون بونكس", "صابون تايد", "كلوركس جالون", "فيري سائل صحون",
                "شامبو برت بلاس", "مناديل فاين رول", "مياه صحية كرتون"
            )
            val suggestions = popularStaples.mapIndexed { index, name ->
                SuggestionEntity(name = name, frequency = 20 - (index % 10))
            }
            dao.insertSuggestions(suggestions)
        }
    }
}
