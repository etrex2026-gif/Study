package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DocumentEntity
import com.example.data.local.entities.UnitEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    documents: List<DocumentEntity>,
    onAddDocument: () -> Unit,
    onSelectDocument: (Long) -> Unit,
    onDeleteDocument: (Long) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Study Librarian", fontWeight = FontWeight.Medium) },
                actions = {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF2E7D32),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                    ) {
                        Text(
                            "OFFLINE",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDocument,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Document")
            }
        }
    ) { padding ->
        if (documents.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.LibraryBooks, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFFFB8C00))
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Your library is empty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Add a book or paste some notes to generate your first study quiz.", style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("ACTIVE SOURCES", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.5.sp)
                }
                items(documents) { doc ->
                    DocumentCard(doc, onClick = { onSelectDocument(doc.id) }, onDelete = { onDeleteDocument(doc.id) })
                }
            }
        }
    }
}

@Composable
fun DocumentCard(doc: DocumentEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = Color(0xFFFFF3E0),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFFB8C00))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(doc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${doc.rawText.length / 500} words • PDF Source", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onImportText: (String, String) -> Unit,
    onImportUri: (Uri, String) -> Unit,
    isAnalyzing: Boolean,
    progress: Float,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var isPasting by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImportUri(uri, title.ifBlank { "New Document" })
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Add Study Material", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Text("SOURCE TYPE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        onClick = { isPasting = true },
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isPasting) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, if (isPasting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, tint = if (isPasting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Text("Paste Text", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Surface(
                        onClick = { pickerLauncher.launch("*/*") },
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (!isPasting) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, if (!isPasting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = if (!isPasting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Text("Upload File", fontWeight = FontWeight.Bold)
                            Text("(PDF, TXT)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }

                if (isPasting) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Paste material here...") },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Button(
                        onClick = { onImportText(title.ifBlank { "Pasted Text" }, text) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = CircleShape,
                        enabled = text.isNotBlank()
                    ) {
                        Text("Process Material", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isAnalyzing) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            onClick = {}
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 8.dp,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                Text(
                                    "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                        Text(
                            "Analyzing Content",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        val status = when {
                            progress < 0.2f -> "Extracting source text..."
                            progress < 0.4f -> "Detecting structure..."
                            progress < 0.6f -> "Identifying topics..."
                            progress < 0.8f -> "Generating question bank..."
                            else -> "Finalizing database..."
                        }
                        Text(
                            status,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
