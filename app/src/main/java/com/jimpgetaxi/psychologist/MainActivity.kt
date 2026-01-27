package com.jimpgetaxi.psychologist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jimpgetaxi.psychologist.presentation.chat.ChatScreen
import com.jimpgetaxi.psychologist.presentation.chat.ChatViewModel
import com.jimpgetaxi.psychologist.presentation.journal.JournalScreen
import com.jimpgetaxi.psychologist.presentation.journal.JournalViewModel
import com.jimpgetaxi.psychologist.presentation.mood.MoodViewModel
import com.jimpgetaxi.psychologist.presentation.sessions.SessionScreen
import com.jimpgetaxi.psychologist.presentation.sessions.SessionViewModel
import com.jimpgetaxi.psychologist.ui.theme.PsychologistTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jimpgetaxi.psychologist.presentation.profile.ProfileSetupScreen
import com.jimpgetaxi.psychologist.presentation.profile.ProfileViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PsychologistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = hiltViewModel()
                    val startDestination by viewModel.startDestination.collectAsState()

                    if (startDestination != "loading") {
                        PsychologistAppNavigation(startDestination)
                    }
                }
            }
        }
    }
}

@Composable
fun PsychologistAppNavigation(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("profile_setup") {
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileSetupScreen(
                viewModel = viewModel,
                onProfileSaved = {
                    navController.navigate("sessions") {
                        popUpTo("profile_setup") { inclusive = true }
                    }
                }
            )
        }
        composable("sessions") {
            val sessionViewModel: SessionViewModel = hiltViewModel()
            val moodViewModel: MoodViewModel = hiltViewModel()
            SessionScreen(
                sessionViewModel = sessionViewModel,
                moodViewModel = moodViewModel,
                onSessionClick = { sessionId ->
                    navController.navigate("chat/$sessionId")
                },
                onJournalClick = {
                    navController.navigate("journal")
                },
                onBreathingClick = {
                    navController.navigate("breathing")
                }
            )
        }
        composable("journal") {
            val viewModel: JournalViewModel = hiltViewModel()
            JournalScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("breathing") {
            val viewModel: com.jimpgetaxi.psychologist.presentation.breathing.BreathingViewModel = hiltViewModel()
            com.jimpgetaxi.psychologist.presentation.breathing.BreathingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "chat/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
            val viewModel: ChatViewModel = hiltViewModel()
            viewModel.setSession(sessionId)
            ChatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}