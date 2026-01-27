package com.jimpgetaxi.psychologist.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AGE = intPreferencesKey("user_age")
        val MAIN_CONCERN = stringPreferencesKey("main_concern")
        val PROFILE_COMPLETED = stringPreferencesKey("profile_completed") // "true" or null
    }

    val userProfile: Flow<UserProfile> = dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[USER_NAME] ?: "",
            age = prefs[USER_AGE] ?: 0,
            mainConcern = prefs[MAIN_CONCERN] ?: "",
            isCompleted = prefs[PROFILE_COMPLETED] == "true"
        )
    }

    suspend fun saveUserProfile(name: String, age: Int, concern: String) {
        dataStore.edit { prefs ->
            prefs[USER_NAME] = name
            prefs[USER_AGE] = age
            prefs[MAIN_CONCERN] = concern
            prefs[PROFILE_COMPLETED] = "true"
        }
    }
    
    suspend fun clearProfile() {
        dataStore.edit { it.clear() }
    }
}

data class UserProfile(
    val name: String,
    val age: Int,
    val mainConcern: String,
    val isCompleted: Boolean
)
