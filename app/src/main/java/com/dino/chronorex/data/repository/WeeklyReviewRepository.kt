package com.dino.chronorex.data.repository

import com.dino.chronorex.data.local.dao.WeeklyReviewDao
import com.dino.chronorex.data.mapper.toEntity
import com.dino.chronorex.data.mapper.toModel
import com.dino.chronorex.model.WeeklyReview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WeeklyReviewRepository(private val weeklyReviewDao: WeeklyReviewDao) {

    fun observeLatest(): Flow<WeeklyReview?> =
        weeklyReviewDao.observeLatest().map { entity -> entity?.toModel() }

    suspend fun getLatest(): WeeklyReview? = weeklyReviewDao.getLatest()?.toModel()

    suspend fun upsert(review: WeeklyReview) {
        weeklyReviewDao.upsert(review.toEntity())
    }

    suspend fun getAll(): List<WeeklyReview> = weeklyReviewDao.getAll().map { it.toModel() }

    suspend fun markNudgeSeen(id: UUID) {
        val current = weeklyReviewDao.getLatest()?.takeIf { it.id == id.toString() } ?: return
        if (!current.needsInAppNudge) return
        weeklyReviewDao.update(current.copy(needsInAppNudge = false))
    }
}
