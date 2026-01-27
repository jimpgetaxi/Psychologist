package com.jimpgetaxi.psychologist.presentation.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimpgetaxi.psychologist.domain.model.JournalEntry
import com.jimpgetaxi.psychologist.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {

    val entries: StateFlow<List<JournalEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveEntry(title: String, content: String) {
        viewModelScope.launch {
            val entry = JournalEntry(
                title = title,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            repository.saveEntry(entry)
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }
}
