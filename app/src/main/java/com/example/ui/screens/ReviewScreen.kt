package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.QuestionEntity

data class QuestionReviewData(
    val question: QuestionEntity,
    val userAnswer: String,
    val isCorrect: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    results: List<QuestionReviewData>,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Quality Review", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    "DETAILED BREAKDOWN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(8.dp))
            }
            
            items(results) { item ->
                ReviewItem(item)
            }
        }
    }
}

@Composable
fun ReviewItem(item: QuestionReviewData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = if (item.isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (item.isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (item.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (item.isCorrect) "Correct" else "Needs Revision",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (item.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    color = when(item.question.difficulty) {
                        "Hard" -> Color(0xFFFFEBEE)
                        "Medium" -> Color(0xFFFFF7E6)
                        else -> Color(0xFFE8F5E9)
                    },
                    shape = CircleShape
                ) {
                    Text(
                        item.question.difficulty.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = when(item.question.difficulty) {
                            "Hard" -> Color(0xFFC62828)
                            "Medium" -> Color(0xFFFB8C00)
                            else -> Color(0xFF2E7D32)
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(item.question.questionText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ResultLabel("Your Answer", item.userAnswer, if (item.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828))
                if (!item.isCorrect) {
                    ResultLabel("Expected Answer", item.question.correctAnswer, Color(0xFF2E7D32))
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Source Reference", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(item.question.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultLabel(label: String, value: String, color: Color) {
    Row {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}
