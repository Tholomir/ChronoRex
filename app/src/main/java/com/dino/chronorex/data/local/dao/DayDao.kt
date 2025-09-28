package com.dino.chronorex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dino.chronorex.data.local.entity.DayEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: DayEntity)

    @Query("SELECT * FROM day WHERE date = :date LIMIT 1")
    suspend fun getDay(date: LocalDate): DayEntity?

    @Query("SELECT * FROM day WHERE date = :date LIMIT 1")
    fun observeDay(date: LocalDate): Flow<DayEntity?>

    @Query("SELECT * FROM day ORDER BY date DESC")
    fun observeAll(): Flow<List<DayEntity>>

    @Query("DELETE FROM day WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)
}
