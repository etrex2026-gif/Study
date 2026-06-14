package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.QuestionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    questions: List<QuestionEntity>,
    isExamMode: Boolean = false,
    onComplete: (Int, Map<Int, String>) -> Unit,
    onBack: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    
    // Timer Logic
    var timeLeft by remember { mutableIntStateOf(questions.size * 60) }
    LaunchedEffect(isExamMode) {
        if (isExamMode) {
            while (timeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                timeLeft--
            }
            // Auto complete on timeout
            var score = 0
            questions.forEachIndexed { index, q ->
                val answer = userAnswers[index] ?: ""
                if (answer.trim().equals(q.correctAnswer.trim(), ignoreCase = true)) {
                    score++
                }
            }
            onComplete(score, userAnswers.toMap())
        }
    }

    if (questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentQuestion = questions[currentIndex]

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = { 
                        Column {
                            Text(if (isExamMode) "Exam Session" else "Study Session", fontWeight = FontWeight.Medium)
                            if (isExamMode) {
                                val mins = timeLeft / 60
                                val secs = timeLeft % 60
                                Text(
                                    String.format("%02d:%02d remaining", mins, secs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (timeLeft < 30) Color.Red else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                    },
                    actions = {
                        IconButton(onClick = { /* Could add hints here if not exam mode */ }) {
                            if (!isExamMode) Icon(Icons.Default.Lightbulb, contentDescription = "Hint")
                        }
                    }
                )
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier.fillMaxWidth().height(4.dp).padding(horizontal = 24.dp),
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = CircleShape,
                        enabled = currentIndex > 0
                    ) {
                        Text("Previous")
                    }
                    Button(
                        onClick = {
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                            } else {
                                var score = 0
                                questions.forEachIndexed { index, q ->
                                    val answer = userAnswers[index] ?: ""
                                    if (answer.trim().equals(q.correctAnswer.trim(), ignoreCase = true)) {
                                        score++
                                    }
                                }
                                onComplete(score, userAnswers.toMap())
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = CircleShape
                    ) {
                        Text(if (currentIndex == questions.size - 1) "Finish Session" else "Next Item")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ITEM ${currentIndex + 1} OF ${questions.size}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                
                Surface(
                    color = when(currentQuestion.difficulty) {
                        "Hard" -> Color(0xFFFFEBEE)
                        "Medium" -> Color(0xFFFFF7E6)
                        else -> Color(0xFFE8F5E9)
                    },
                    shape = CircleShape
                ) {
                    Text(
                        currentQuestion.difficulty.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = when(currentQuestion.difficulty) {
                            "Hard" -> Color(0xFFC62828)
                            "Medium" -> Color(0xFFFB8C00)
                            else -> Color(0xFF2E7D32)
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Text(
                    text = currentQuestion.questionText,
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp
                )
            }

            when (currentQuestion.type) {
                "MCQ", "TRUE_FALSE" -> {
                    val options = remember(currentQuestion) {
                        (currentQuestion.distractors?.split("|") ?: emptyList())
                            .plus(currentQuestion.correctAnswer)
                            .shuffled()
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        options.forEach { option ->
                            val isSelected = userAnswers[currentIndex] == option
                            Surface(
                                onClick = { userAnswers[currentIndex] = option },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(
                                    2.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    } else {
                                        Box(Modifier.size(24.dp).border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text(option, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
                "FILL_BLANKS", "EXPLAIN" -> {
                    OutlinedTextField(
                        value = userAnswers[currentIndex] ?: "",
                        onValueChange = { userAnswers[currentIndex] = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter your response here") },
                        minLines = 4,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}
