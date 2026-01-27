package com.jimpgetaxi.psychologist.presentation.breathing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BreathingPhase(val instruction: String, val durationMillis: Long) {
    IDLE("Tap Start", 0),
    INHALE("Inhale...", 4000),
    HOLD_FULL("Hold...", 4000),
    EXHALE("Exhale...", 4000),
    HOLD_EMPTY("Hold...", 4000)
}

@HiltViewModel
class BreathingViewModel @Inject constructor() : ViewModel() {

    private val _currentPhase = MutableStateFlow(BreathingPhase.IDLE)
    val currentPhase: StateFlow<BreathingPhase> = _currentPhase.asStateFlow()

    private var breathingJob: Job? = null

    fun toggleBreathing() {
        if (breathingJob?.isActive == true) {
            stopBreathing()
        } else {
            startBreathing()
        }
    }

    private fun startBreathing() {
        breathingJob = viewModelScope.launch {
            while (true) {
                // Inhale
                _currentPhase.value = BreathingPhase.INHALE
                delay(BreathingPhase.INHALE.durationMillis)
                
                // Hold Full
                _currentPhase.value = BreathingPhase.HOLD_FULL
                delay(BreathingPhase.HOLD_FULL.durationMillis)
                
                // Exhale
                _currentPhase.value = BreathingPhase.EXHALE
                delay(BreathingPhase.EXHALE.durationMillis)
                
                // Hold Empty
                _currentPhase.value = BreathingPhase.HOLD_EMPTY
                delay(BreathingPhase.HOLD_EMPTY.durationMillis)
            }
        }
    }

    private fun stopBreathing() {
        breathingJob?.cancel()
        _currentPhase.value = BreathingPhase.IDLE
    }

    override fun onCleared() {
        super.onCleared()
        stopBreathing()
    }
}
