package com.jimpgetaxi.psychologist.presentation.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimpgetaxi.psychologist.data.local.MoodEntity
import com.jimpgetaxi.psychologist.domain.repository.MoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val repository: MoodRepository
) : ViewModel() {

    val allMoods: StateFlow<List<MoodEntity>> = repository.getAllMoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveMood(moodValue: Int, note: String? = null) {
        viewModelScope.launch {
            repository.saveMood(moodValue, note)
        }
    }
}
