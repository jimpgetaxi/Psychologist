package com.jimpgetaxi.psychologist.presentation.breathing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    viewModel: BreathingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val currentPhase by viewModel.currentPhase.collectAsState()
    
    // Animation state
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(currentPhase) {
        when (currentPhase) {
            BreathingPhase.IDLE -> scale.snapTo(1f)
            BreathingPhase.INHALE -> {
                scale.animateTo(
                    targetValue = 2.0f,
                    animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
                )
            }
            BreathingPhase.EXHALE -> {
                scale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
                )
            }
            else -> {
                // For HOLD phases, we just keep the current scale
                // No action needed as animatable holds its value
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathing Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                // Outer circle guide
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Animated breathing circle
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawCircle(
                        color = Color(0xFF81C784), // Soft green
                        radius = size.minDimension / 2 * scale.value
                    )
                }
                
                Text(
                    text = currentPhase.instruction,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(onClick = { viewModel.toggleBreathing() }) {
                Text(text = if (currentPhase == BreathingPhase.IDLE) "Start" else "Stop")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Box Breathing: 4s Inhale, 4s Hold, 4s Exhale, 4s Hold",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
