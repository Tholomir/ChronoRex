package com.dino.chronorex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dino.chronorex.data.local.converter.ChronoRexConverters
import com.dino.chronorex.data.local.dao.ActivityEntryDao
import com.dino.chronorex.data.local.dao.DayDao
import com.dino.chronorex.data.local.dao.SettingsDao
import com.dino.chronorex.data.local.dao.SymptomEntryDao
import com.dino.chronorex.data.local.dao.WeeklyReviewDao
import com.dino.chronorex.data.local.entity.ActivityEntryEntity
import com.dino.chronorex.data.local.entity.DayEntity
import com.dino.chronorex.data.local.entity.SettingsEntity
import com.dino.chronorex.data.local.entity.SymptomEntryEntity
import com.dino.chronorex.data.local.entity.WeeklyReviewEntity

@Database(
    entities = [
        DayEntity::class,
        SymptomEntryEntity::class,
        ActivityEntryEntity::class,
        SettingsEntity::class,
        WeeklyReviewEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(ChronoRexConverters::class)
abstract class ChronoRexDatabase : RoomDatabase() {
    abstract fun dayDao(): DayDao
    abstract fun symptomEntryDao(): SymptomEntryDao
    abstract fun activityEntryDao(): ActivityEntryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun weeklyReviewDao(): WeeklyReviewDao

    companion object {
        @Volatile
        private var INSTANCE: ChronoRexDatabase? = null

        fun getInstance(context: Context): ChronoRexDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): ChronoRexDatabase =
            Room.databaseBuilder(
                context,
                ChronoRexDatabase::class.java,
                "chronorex.db"
            ).fallbackToDestructiveMigration().build()
    }
}
