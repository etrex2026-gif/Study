package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.QuizViewModel
import com.example.ui.screens.*
import com.example.ui.screens.QuestionReviewData
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        val viewModel: QuizViewModel = viewModel()
        
        NavHost(navController = navController, startDestination = "home") {
          composable("home") {
            val docs by viewModel.documents.collectAsState(emptyList())
            HomeScreen(
              documents = docs,
              onAddDocument = { navController.navigate("import") },
              onSelectDocument = { id -> 
                viewModel.loadUnits(id)
                navController.navigate("units/$id") 
              },
              onDeleteDocument = { viewModel.deleteDocument(it) }
            )
          }
          
          composable("import") {
            val isAnalyzing by viewModel.isAnalyzing.collectAsState()
            val progress by viewModel.analysisProgress.collectAsState()
            ImportScreen(
              onImportText = { title, text ->
                viewModel.importText(title, text) { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
              },
              onImportUri = { uri, title ->
                viewModel.importUri(uri, title) { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
              },
              isAnalyzing = isAnalyzing,
              progress = progress,
              onBack = { navController.popBackStack() }
            )
          }
          
          composable(
            "units/{docId}",
            arguments = listOf(navArgument("docId") { type = NavType.LongType })
          ) { backStackEntry ->
            val units by viewModel.currentUnits.collectAsState()
            UnitSelectionScreen(
              units = units,
              onUnitsSelected = { ids -> 
                val docId = backStackEntry.arguments?.getLong("docId") ?: 0L
                navController.navigate("config/$docId/${ids.joinToString(",")}")
              },
              onBack = { navController.popBackStack() }
            )
          }
          
          composable(
            "config/{docId}/{unitIds}",
            arguments = listOf(
              navArgument("docId") { type = NavType.LongType },
              navArgument("unitIds") { type = NavType.StringType }
            )
          ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getLong("docId") ?: 0L
            val unitIdsStr = backStackEntry.arguments?.getString("unitIds") ?: ""
            val unitIds = unitIdsStr.split(",").map { it.toLong() }
            
            QuizConfigScreen(
              onStartQuiz = { types, count, isExam, difficulty ->
                viewModel.generateQuiz(unitIds, types, count, difficulty)
                navController.navigate("quiz/$docId/$isExam")
              },
              onBack = { navController.popBackStack() }
            )
          }
          
          composable(
            "quiz/{docId}/{isExam}",
            arguments = listOf(
              navArgument("docId") { type = NavType.LongType },
              navArgument("isExam") { type = NavType.BoolType }
            )
          ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getLong("docId") ?: 0L
            val isExam = backStackEntry.arguments?.getBoolean("isExam") ?: false
            val questions by viewModel.generatedQuestions.collectAsState()
            
            QuizScreen(
              questions = questions,
              isExamMode = isExam,
              onComplete = { score, responses ->
                viewModel.saveResults(docId, score, questions.size, responses)
                navController.navigate("results/$score/${questions.size}")
              },
              onBack = { navController.popBackStack() }
            )
          }
          
          composable(
            "results/{score}/{total}",
            arguments = listOf(
              navArgument("score") { type = NavType.IntType },
              navArgument("total") { type = NavType.IntType }
            )
          ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            ResultsScreen(
              score = score,
              total = total,
              onHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
              onReview = { navController.navigate("review") }
            )
          }
          
          composable("review") {
            val questions by viewModel.generatedQuestions.collectAsState()
            val lastResults by viewModel.lastQuizResults.collectAsState()
            
            val reviewData = lastResults.mapNotNull { result ->
                val question = questions.find { it.id == result.questionId }
                if (question != null) {
                    QuestionReviewData(question, result.userAnswer, result.isCorrect)
                } else null
            }
            
            ReviewScreen(
                results = reviewData,
                onBack = { navController.popBackStack() }
            )
          }

        }
      }
    }
  }
}
