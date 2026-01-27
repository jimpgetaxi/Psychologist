package com.jimpgetaxi.psychologist.presentation.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jimpgetaxi.psychologist.domain.model.JournalEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ημερολόγιο Σκέψεων") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Δεν υπάρχουν εγγραφές ακόμα. Ξεκινήστε να γράφετε τις σκέψεις σας.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries) { entry ->
                    JournalEntryItem(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry) }
                    )
                }
            }
        }

        if (showDialog) {
            AddJournalEntryDialog(
                onDismiss = { showDialog = false },
                onSave = { title, content ->
                    viewModel.saveEntry(title, content)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun JournalEntryItem(
    entry: JournalEntry,
    onDelete: () -> Unit
) {
    val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(entry.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = entry.content)
        }
    }
}

@Composable
fun AddJournalEntryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Νέα Σκέψη") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Τίτλος") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Τι σκέφτεστε;") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (content.isNotBlank()) onSave(title.ifBlank { "Χωρίς Τίτλο" }, content) },
                enabled = content.isNotBlank()
            ) {
                Text("Αποθήκευση")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ακύρωση")
            }
        }
    )
}
