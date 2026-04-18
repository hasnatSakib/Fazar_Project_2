package com.example.fazarproject2.domain.usecase

import com.example.fazarproject2.domain.model.AlarmSettings
import com.example.fazarproject2.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Use case to update alarm settings.
 */
class UpdateAlarmSettingsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(settings: AlarmSettings) {
        repository.updateAlarmSettings(settings)
    }
}
