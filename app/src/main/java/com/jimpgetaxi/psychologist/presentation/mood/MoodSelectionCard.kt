package com.jimpgetaxi.psychologist.presentation.mood

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MoodSelectionCard(
    onMoodSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Πώς αισθάνεσαι σήμερα;",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MoodEmojiButton(emoji = "😞", value = 1, onClick = onMoodSelected)
                MoodEmojiButton(emoji = "😟", value = 2, onClick = onMoodSelected)
                MoodEmojiButton(emoji = "😐", value = 3, onClick = onMoodSelected)
                MoodEmojiButton(emoji = "🙂", value = 4, onClick = onMoodSelected)
                MoodEmojiButton(emoji = "😄", value = 5, onClick = onMoodSelected)
            }
        }
    }
}

@Composable
fun MoodEmojiButton(
    emoji: String,
    value: Int,
    onClick: (Int) -> Unit
) {
    IconButton(
        onClick = { onClick(value) },
        modifier = Modifier.size(56.dp)
    ) {
        Text(text = emoji, fontSize = 32.sp)
    }
}
