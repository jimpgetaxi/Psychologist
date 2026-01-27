package com.jimpgetaxi.psychologist

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jimpgetaxi.psychologist.presentation.auth.BiometricPromptManager
import com.jimpgetaxi.psychologist.presentation.chat.ChatScreen
import com.jimpgetaxi.psychologist.presentation.chat.ChatViewModel
import com.jimpgetaxi.psychologist.presentation.journal.JournalScreen
import com.jimpgetaxi.psychologist.presentation.journal.JournalViewModel
import com.jimpgetaxi.psychologist.presentation.mood.MoodViewModel
import com.jimpgetaxi.psychologist.presentation.profile.ProfileSetupScreen
import com.jimpgetaxi.psychologist.presentation.profile.ProfileViewModel
import com.jimpgetaxi.psychologist.presentation.sessions.SessionScreen
import com.jimpgetaxi.psychologist.presentation.sessions.SessionViewModel
import com.jimpgetaxi.psychologist.ui.theme.PsychologistTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private val promptManager by lazy {
        BiometricPromptManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PsychologistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isAuthenticated by remember { mutableStateOf(false) }
                    val biometricResult by promptManager.promptResults.collectAsState(initial = null)
                    
                    LaunchedEffect(biometricResult) {
                        if (biometricResult is BiometricPromptManager.BiometricResult.AuthenticationSuccess) {
                            isAuthenticated = true
                        }
                    }

                    // Auto-show prompt on start if not authenticated
                    LaunchedEffect(Unit) {
                        if (!isAuthenticated) {
                            promptManager.showBiometricPrompt(
                                title = "Psychologist App Locked",
                                description = "Confirm your identity to access your private sessions."
                            )
                        }
                    }

                    if (isAuthenticated) {
                        val viewModel: MainViewModel = hiltViewModel()
                        val startDestination by viewModel.startDestination.collectAsState()

                        if (startDestination != "loading") {
                            PsychologistAppNavigation(startDestination)
                        }
                    } else {
                        // Locked Screen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Locked",
                                    style = MaterialTheme.typography.headlineLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = {
                                    promptManager.showBiometricPrompt(
                                        title = "Psychologist App Locked",
                                        description = "Confirm your identity to access your private sessions."
                                    )
                                }) {
                                    Text("Unlock")
                                }
                                
                                if (biometricResult is BiometricPromptManager.BiometricResult.AuthenticationError) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = (biometricResult as BiometricPromptManager.BiometricResult.AuthenticationError).error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                
                                if (biometricResult is BiometricPromptManager.BiometricResult.AuthenticationNotSet) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Biometric authentication is not set up on this device.")
                                    Button(onClick = {
                                        if (Build.VERSION.SDK_INT >= 30) {
                                            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                                            }
                                            startActivity(enrollIntent)
                                        }
                                    }) {
                                        Text("Setup Security")
                                    }
                                    // Fallback mainly for emulator or devices without secure lock screen
                                    // In a real app, you might force setup or have a pin fallback within app
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { isAuthenticated = true }) {
                                        Text("Bypass (Dev Only if no hardware)")
                                    }
                                }
                            }
                        }
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
