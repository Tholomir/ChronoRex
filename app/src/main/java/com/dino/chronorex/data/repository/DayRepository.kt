package com.dino.chronorex.data.repository

import com.dino.chronorex.data.local.dao.DayDao
import com.dino.chronorex.data.mapper.toEntity
import com.dino.chronorex.data.mapper.toModel
import com.dino.chronorex.model.Day
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class DayRepository(private val dayDao: DayDao) {
    fun observeDay(date: LocalDate): Flow<Day?> =
        dayDao.observeDay(date).map { entity -> entity?.toModel() }

    fun observeAll(): Flow<List<Day>> =
        dayDao.observeAll().map { entities -> entities.map { it.toModel() } }

    suspend fun getDay(date: LocalDate): Day? = dayDao.getDay(date)?.toModel()

    suspend fun upsert(day: Day) {
        dayDao.upsert(day.toEntity())
    }

    suspend fun deleteByDate(date: LocalDate) {
        dayDao.deleteByDate(date)
    }
}
