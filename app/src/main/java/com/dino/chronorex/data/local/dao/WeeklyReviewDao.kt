package com.dino.chronorex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dino.chronorex.data.local.entity.WeeklyReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(review: WeeklyReviewEntity)

    @Update
    suspend fun update(review: WeeklyReviewEntity)

    @Query("SELECT * FROM weekly_review ORDER BY generated_at DESC LIMIT 1")
    fun observeLatest(): Flow<WeeklyReviewEntity?>

    @Query("SELECT * FROM weekly_review ORDER BY generated_at DESC LIMIT 1")
    suspend fun getLatest(): WeeklyReviewEntity?

    @Query("SELECT * FROM weekly_review ORDER BY generated_at DESC")
    suspend fun getAll(): List<WeeklyReviewEntity>
}
