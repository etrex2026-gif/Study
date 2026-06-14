package com.example.ui.screens

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UnitEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelectionScreen(
    units: List<UnitEntity>,
    onUnitsSelected: (List<Long>) -> Unit,
    onBack: () -> Unit
) {
    val selectedIds = remember { mutableStateListOf<Long>() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Select Topics", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    Text(
                        "${selectedIds.size} selected",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { onUnitsSelected(selectedIds.toList()) },
                    modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp),
                    shape = CircleShape,
                    enabled = selectedIds.isNotEmpty()
                ) {
                    Text("Configure Quiz Format", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("TOPICS DETECTED", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(8.dp))
            }
            items(units) { unit ->
                val isSelected = selectedIds.contains(unit.id)
                Surface(
                    onClick = {
                        if (isSelected) selectedIds.remove(unit.id)
                        else selectedIds.add(unit.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            unit.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        } else {
                            Box(Modifier.size(24.dp).border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigScreen(
    onStartQuiz: (List<String>, Int, Boolean, String?) -> Unit,
    onBack: () -> Unit
) {
    var countPerType by remember { mutableFloatStateOf(5f) }
    val selectedTypes = remember { mutableStateListOf("MCQ", "TRUE_FALSE") }
    var isExamMode by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Quiz Format", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("QUESTION TYPES", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            val types = listOf(
                "MCQ" to "Multiple Choice",
                "TRUE_FALSE" to "True / False",
                "FILL_BLANKS" to "Fill in the Blanks"
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                types.forEach { (key, label) ->
                    QuizTypeCard(
                        label = label,
                        type = key,
                        isSelected = selectedTypes.contains(key),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (selectedTypes.contains(key)) {
                            if (selectedTypes.size > 1) selectedTypes.remove(key)
                        } else {
                            selectedTypes.add(key)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = if (isExamMode) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isExamMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                onClick = { isExamMode = !isExamMode }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = if (isExamMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isExamMode) Icons.Default.Timer else Icons.Default.TimerOff,
                                contentDescription = null,
                                tint = if (isExamMode) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Exam Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Adds timer and disables hints", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(checked = isExamMode, onCheckedChange = { isExamMode = it })
                }
            }

            Text("ITEMS PER TYPE: ${countPerType.toInt()}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Slider(
                value = countPerType,
                onValueChange = { countPerType = it },
                valueRange = 1f..15f,
                steps = 14
            )

            Text("DIFFICULTY FILTER", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Easy", "Medium", "Hard").forEach { level ->
                    val isLevelSelected = (selectedDifficulty == null && level == "All") || (selectedDifficulty == level)
                    FilterChip(
                        selected = isLevelSelected,
                        onClick = { selectedDifficulty = if (level == "All") null else level },
                        label = { Text(level) }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onStartQuiz(selectedTypes.toList(), countPerType.toInt(), isExamMode, selectedDifficulty) },
                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, CircleShape),
                shape = CircleShape,
                enabled = selectedTypes.isNotEmpty()
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Launch ${if (isExamMode) "Exam" else "Quiz"}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuizTypeCard(label: String, type: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
        border = BorderStroke(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(type, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
