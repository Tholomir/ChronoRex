package com.dino.chronorex.data.repository

import com.dino.chronorex.data.local.dao.SymptomEntryDao
import com.dino.chronorex.data.mapper.toEntity
import com.dino.chronorex.data.mapper.toModel
import com.dino.chronorex.model.SymptomEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

class SymptomRepository(private val symptomEntryDao: SymptomEntryDao) {
    fun observeSymptomsForDate(date: LocalDate): Flow<List<SymptomEntry>> =
        symptomEntryDao.observeByDate(date).map { list -> list.map { it.toModel() } }

    fun observeAll(): Flow<List<SymptomEntry>> =
        symptomEntryDao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun upsert(entry: SymptomEntry) {
        symptomEntryDao.upsert(entry.toEntity())
    }

    suspend fun delete(id: UUID) {
        symptomEntryDao.getById(id.toString())?.let { symptomEntryDao.delete(it) }
    }
}
