package com.jimpgetaxi.psychologist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimpgetaxi.psychologist.data.local.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: UserPreferencesRepository
) : ViewModel() {
    val startDestination: StateFlow<String> = repository.userProfile
        .map { if (it.isCompleted) "sessions" else "profile_setup" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "loading")
}
