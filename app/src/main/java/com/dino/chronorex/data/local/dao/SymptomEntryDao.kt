package com.dino.chronorex.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dino.chronorex.data.local.entity.SymptomEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SymptomEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SymptomEntryEntity)

    @Update
    suspend fun update(entry: SymptomEntryEntity)

    @Delete
    suspend fun delete(entry: SymptomEntryEntity)

    @Query("SELECT * FROM symptom_entry WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SymptomEntryEntity?

    @Query("SELECT * FROM symptom_entry WHERE date = :date ORDER BY time DESC")
    fun observeByDate(date: LocalDate): Flow<List<SymptomEntryEntity>>

    @Query("SELECT * FROM symptom_entry ORDER BY time DESC")
    fun observeAll(): Flow<List<SymptomEntryEntity>>
}
