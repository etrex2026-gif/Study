package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    score: Int,
    total: Int,
    onHome: () -> Unit,
    onReview: () -> Unit
) {
    val percentage = if (total > 0) (score.toFloat() / total * 100).toInt() else 0
    val feedback = when {
        percentage >= 90 -> "SENSATIONAL"
        percentage >= 75 -> "GREAT EFFORT"
        percentage >= 50 -> "KEEP GOING"
        else -> "STUDY MORE"
    }
    
    val feedbackSub = when {
        percentage >= 90 -> "You've mastered these concepts perfectly."
        percentage >= 75 -> "You have a solid grasp on the material."
        percentage >= 50 -> "You're getting there! A bit more review will help."
        else -> "Don't get discouraged. Study the units again and retry."
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Session Summary", fontWeight = FontWeight.Medium) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = {}
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$percentage%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text("SCORE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            Text(feedback, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text(feedbackSub, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            
            Spacer(Modifier.height(32.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFFB8C00), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Accuracy Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Answered $score out of $total questions correctly.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            Spacer(Modifier.height(64.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onReview,
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = CircleShape
                ) {
                    Text("REVIEW MISTAKES", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onHome,
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("DONE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
