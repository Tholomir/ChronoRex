package com.dino.chronorex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dino.chronorex.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsEntity)

    @Query("SELECT * FROM settings WHERE id = :id LIMIT 1")
    suspend fun getSettings(id: Int = SettingsEntity.SINGLETON_ID): SettingsEntity?

    @Query("SELECT * FROM settings WHERE id = :id LIMIT 1")
    fun observeSettings(id: Int = SettingsEntity.SINGLETON_ID): Flow<SettingsEntity?>
}
