package com.jimpgetaxi.psychologist.presentation.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jimpgetaxi.psychologist.R
import com.jimpgetaxi.psychologist.data.local.SessionEntity
import com.jimpgetaxi.psychologist.presentation.mood.MoodSelectionCard
import com.jimpgetaxi.psychologist.presentation.mood.MoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    sessionViewModel: SessionViewModel,
    moodViewModel: MoodViewModel,
    onSessionClick: (Long) -> Unit,
    onJournalClick: () -> Unit,
    onBreathingClick: () -> Unit
) {
    val sessions by sessionViewModel.sessions.collectAsState()
    val allMoods by moodViewModel.allMoods.collectAsState()
    var showRenameDialog by remember { mutableStateOf<SessionEntity?>(null) }

    if (showRenameDialog != null) {
        RenameSessionDialog(
            session = showRenameDialog!!,
            onDismiss = { showRenameDialog = null },
            onConfirm = { newTitle ->
                sessionViewModel.updateSessionTitle(showRenameDialog!!.id, newTitle)
                showRenameDialog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_sessions), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onBreathingClick) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Breathing Exercises")
                    }
                    IconButton(onClick = onJournalClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Journal")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                sessionViewModel.createSession { id -> onSessionClick(id) }
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_session))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                MoodSelectionCard(
                    onMoodSelected = { value ->
                        moodViewModel.saveMood(value)
                    }
                )
                if (allMoods.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    com.jimpgetaxi.psychologist.presentation.mood.MoodChart(moods = allMoods)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.recent_sessions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(sessions) { session ->
                SessionItem(
                    session = session,
                    onClick = onSessionClick,
                    onDelete = { sessionViewModel.deleteSession(session.id) },
                    onRename = { showRenameDialog = session }
                )
            }
        }
    }
}

@Composable
fun SessionItem(
    session: SessionEntity,
    onClick: (Long) -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(session.id) },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title ?: "${stringResource(R.string.session_prefix)}${session.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(session.startTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            expanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RenameSessionDialog(
    session: SessionEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(session.title ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Session") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Session Title") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}