package com.jimpgetaxi.psychologist.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimpgetaxi.psychologist.data.local.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    fun saveProfile(name: String, age: String, concern: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            val ageInt = age.toIntOrNull() ?: 0
            repository.saveUserProfile(name, ageInt, concern)
            onSaved()
        }
    }
}
