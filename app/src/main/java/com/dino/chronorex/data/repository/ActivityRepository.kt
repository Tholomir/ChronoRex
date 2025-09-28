package com.dino.chronorex.data.repository

import com.dino.chronorex.data.local.dao.ActivityEntryDao
import com.dino.chronorex.data.mapper.toEntity
import com.dino.chronorex.data.mapper.toModel
import com.dino.chronorex.model.ActivityEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

class ActivityRepository(private val activityEntryDao: ActivityEntryDao) {
    fun observeActivitiesForDate(date: LocalDate): Flow<List<ActivityEntry>> =
        activityEntryDao.observeByDate(date).map { list -> list.map { it.toModel() } }

    fun observeAll(): Flow<List<ActivityEntry>> =
        activityEntryDao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun upsert(entry: ActivityEntry) {
        activityEntryDao.upsert(entry.toEntity())
    }

    suspend fun delete(id: UUID) {
        activityEntryDao.getById(id.toString())?.let { activityEntryDao.delete(it) }
    }
}
