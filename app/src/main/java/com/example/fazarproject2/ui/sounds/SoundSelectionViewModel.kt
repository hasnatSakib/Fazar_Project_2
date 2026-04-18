package com.example.fazarproject2.ui.sounds

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fazarproject2.domain.model.AudioFile
import com.example.fazarproject2.domain.repository.AlarmRepository
import com.example.fazarproject2.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundSelectionViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val TAG = "SoundSelectionViewModel"

    private val _systemSounds = MutableStateFlow<List<AudioFile>>(emptyList())
    val systemSounds: StateFlow<List<AudioFile>> = _systemSounds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val myCollection: StateFlow<List<AudioFile>> = mediaRepository.getSavedAudio()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alarmSettings = alarmRepository.getAlarmSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun scanSystemSounds() {
        Log.d(TAG, "scanSystemSounds: Starting scan")
        println("$TAG: scanSystemSounds: Starting scan")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _systemSounds.value = mediaRepository.fetchSystemAudio()
                Log.d(TAG, "scanSystemSounds: Scan complete, found ${_systemSounds.value.size} sounds")
                println("$TAG: scanSystemSounds: Scan complete, found ${_systemSounds.value.size} sounds")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handleFilePicked(uri: Uri, fileName: String?) {
        Log.d(TAG, "handleFilePicked: uri = $uri, name = $fileName")
        println("$TAG: handleFilePicked: uri = $uri, name = $fileName")
        viewModelScope.launch {
            // By specification, we add it to the collection and potentially select it
            val title = fileName ?: "Picked Sound"
            val audioFile = AudioFile(
                uri = uri.toString(),
                title = title,
                durationMs = 0 // Duration might not be known immediately for picked files
            )
            mediaRepository.saveAudio(audioFile)
            selectAsActive(audioFile.uri, audioFile.title)
        }
    }

    fun addToCollection(audioFile: AudioFile) {
        Log.d(TAG, "addToCollection: Adding ${audioFile.title}")
        println("$TAG: addToCollection: Adding ${audioFile.title}")
        viewModelScope.launch {
            mediaRepository.saveAudio(audioFile)
        }
    }

    fun removeFromCollection(audioFile: AudioFile) {
        Log.d(TAG, "removeFromCollection: Removing ${audioFile.title}")
        println("$TAG: removeFromCollection: Removing ${audioFile.title}")
        viewModelScope.launch {
            mediaRepository.deleteAudio(audioFile)
        }
    }

    fun selectAsActive(uri: String, title: String) {
        Log.d(TAG, "selectAsActive: Selecting $title as active alarm")
        println("$TAG: selectAsActive: Selecting $title as active alarm")
        viewModelScope.launch {
            val settings = alarmSettings.value ?: return@launch
            alarmRepository.updateAlarmSettings(
                settings.copy(
                    customAudioUri = uri,
                    customAudioFileName = title
                )
            )
        }
    }
}
