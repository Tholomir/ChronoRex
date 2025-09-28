package com.dino.chronorex.data.repository

import com.dino.chronorex.data.local.dao.SettingsDao
import com.dino.chronorex.data.mapper.toEntity
import com.dino.chronorex.data.mapper.toModel
import com.dino.chronorex.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDao: SettingsDao) {
    fun observeSettings(): Flow<Settings> =
        settingsDao.observeSettings().map { entity -> entity?.toModel() ?: Settings.default() }

    suspend fun loadSettings(): Settings {
        val existing = settingsDao.getSettings()
        return if (existing != null) {
            existing.toModel()
        } else {
            val defaults = Settings.default()
            settingsDao.upsert(defaults.toEntity())
            defaults
        }
    }

    suspend fun save(settings: Settings) {
        settingsDao.upsert(settings.toEntity())
    }

    suspend fun update(transform: (Settings) -> Settings) {
        val current = loadSettings()
        save(transform(current))
    }
}
