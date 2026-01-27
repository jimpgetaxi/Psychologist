package com.jimpgetaxi.psychologist.presentation.mood

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jimpgetaxi.psychologist.data.local.MoodEntity
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MoodChart(
    moods: List<MoodEntity>,
    modifier: Modifier = Modifier
) {
    if (moods.isEmpty()) return

    // Take last 7 moods and reverse them to show chronologically (Oldest -> Newest)
    val displayMoods = remember(moods) { moods.take(10).reversed() }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Mood Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(8.dp)
            ) {
                val width = size.width
                val height = size.height
                val spacePerPoint = width / (displayMoods.size + 1)
                
                // Y-axis logic: Value 1 is bottom, 5 is top.
                // Map 1..5 to height..0
                fun getY(value: Int): Float {
                    val normalized = (value - 1) / 4f // 0.0 to 1.0
                    return height - (normalized * height)
                }

                // Draw Grid Lines (1 to 5)
                for (i in 1..5) {
                    val y = getY(i)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (displayMoods.isEmpty()) return@Canvas

                val path = Path()
                var previousPoint: Offset? = null

                displayMoods.forEachIndexed { index, mood ->
                    val x = (index + 1) * spacePerPoint
                    val y = getY(mood.moodValue)
                    val point = Offset(x, y)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        // Bezier curve for smoother look could go here, but straight lines are clearer for discreet data
                        path.lineTo(x, y)
                    }

                    // Draw Point
                    drawCircle(
                        color = when(mood.moodValue) {
                            1 -> Color(0xFFEF5350) // Red
                            2 -> Color(0xFFFFAB91) // Light Red
                            3 -> Color(0xFFFFD54F) // Yellow
                            4 -> Color(0xFFAED581) // Light Green
                            5 -> Color(0xFF66BB6A) // Green
                            else -> Color.Gray
                        },
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    
                    previousPoint = point
                }

                // Draw Line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
            
            Text(
                text = "Last ${displayMoods.size} entries",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
