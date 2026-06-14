package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.repository.QuizRepository
import com.example.domain.analyzer.ContentAnalyzer
import com.example.domain.generator.QuestionGenerator
import com.example.domain.parser.DocumentParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class QuestionResult(
    val questionId: Long,
    val userAnswer: String,
    val isCorrect: Boolean
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "studygen_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = QuizRepository(
        dao = db.quizDao(),
        parser = DocumentParser(application),
        analyzer = ContentAnalyzer(),
        generator = QuestionGenerator()
    )

    val documents = repository.documents
    val sessions = repository.sessions

    private val _currentUnits = MutableStateFlow<List<UnitEntity>>(emptyList())
    val currentUnits: StateFlow<List<UnitEntity>> = _currentUnits.asStateFlow()

    private val _generatedQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val generatedQuestions: StateFlow<List<QuestionEntity>> = _generatedQuestions.asStateFlow()

    private val _lastQuizResults = MutableStateFlow<List<QuestionResult>>(emptyList())
    val lastQuizResults: StateFlow<List<QuestionResult>> = _lastQuizResults.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun loadUnits(docId: Long) {
        viewModelScope.launch {
            repository.getUnits(docId).collect {
                _currentUnits.value = it
            }
        }
    }

    fun importText(title: String, text: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            simulateProgress()
            val id = repository.addDocumentFromText(title, text)
            _isAnalyzing.value = false
            onComplete(id)
        }
    }

    fun importUri(uri: android.net.Uri, title: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            simulateProgress()
            val id = repository.addDocumentFromUri(uri, title)
            _isAnalyzing.value = false
            onComplete(id)
        }
    }

    private suspend fun simulateProgress() {
        val steps = listOf(
            0.1f to "Extracting text...",
            0.3f to "Detecting structure...",
            0.5f to "Identifying topics...",
            0.7f to "Extracting key facts...",
            0.9f to "Generating question bank...",
            1.0f to "Finalizing database..."
        )
        for (step in steps) {
            _analysisProgress.value = step.first
            delay(500) // Simulate processing time
        }
    }

    fun generateQuiz(unitIds: List<Long>, types: List<String>, countPerType: Int) {
        viewModelScope.launch {
            val generator = QuestionGenerator()
            val existingQuestions = repository.getQuestionsForUnits(unitIds)
            
            val filteredExisting = existingQuestions.filter { types.contains(it.type) }
            
            // If we have enough stored questions, use them
            if (filteredExisting.size >= (types.size * countPerType)) {
                _generatedQuestions.value = filteredExisting.shuffled().take(types.size * countPerType)
            } else {
                // Generate and save more
                val questions = mutableListOf<QuestionEntity>()
                val unitsToProcess = _currentUnits.value.filter { it.id in unitIds }
                
                for (unit in unitsToProcess) {
                    questions.addAll(generator.generate(unit.id, unit.content, types, countPerType))
                }
                
                // Save to DB for future offline-instancy
                db.quizDao().insertQuestions(questions)
                _generatedQuestions.value = questions.shuffled()
            }
        }
    }

    fun saveResults(docId: Long, score: Int, total: Int, responses: Map<Int, String>) {
        viewModelScope.launch {
            val questions = _generatedQuestions.value
            val results = questions.mapIndexed { index, question ->
                val answer = responses[index] ?: ""
                QuestionResult(
                    questionId = question.id,
                    userAnswer = answer,
                    isCorrect = answer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                )
            }
            
            _lastQuizResults.value = results
            val resultsJson = Json.encodeToString(results)

            repository.saveSession(QuizSessionEntity(
                documentId = docId,
                score = score,
                totalQuestions = total,
                detailsJson = resultsJson
            ))
        }
    }



    fun deleteDocument(id: Long) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }
}
