package com.dino.chronorex.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dino.chronorex.data.local.entity.ActivityEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ActivityEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ActivityEntryEntity)

    @Update
    suspend fun update(entry: ActivityEntryEntity)

    @Delete
    suspend fun delete(entry: ActivityEntryEntity)

    @Query("SELECT * FROM activity_entry WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ActivityEntryEntity?

    @Query("SELECT * FROM activity_entry WHERE date = :date ORDER BY time DESC")
    fun observeByDate(date: LocalDate): Flow<List<ActivityEntryEntity>>

    @Query("SELECT * FROM activity_entry ORDER BY time DESC")
    fun observeAll(): Flow<List<ActivityEntryEntity>>
}
