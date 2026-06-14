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
            val id = repository.addDocumentFromText(title, text)
            val units = db.quizDao().getUnitsForDocumentSync(id)
            syncPhasedAnalysis(units)
            _isAnalyzing.value = false
            onComplete(id)
        }
    }

    fun importUri(uri: android.net.Uri, title: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val id = repository.addDocumentFromUri(uri, title)
            val units = db.quizDao().getUnitsForDocumentSync(id)
            syncPhasedAnalysis(units)
            _isAnalyzing.value = false
            onComplete(id)
        }
    }

    private suspend fun syncPhasedAnalysis(units: List<UnitEntity>) {
        val generator = QuestionGenerator()
        val types = listOf("MCQ", "TRUE_FALSE", "FILL_BLANKS")
        
        val progressStages = listOf(
            0.1f to "Reading document...",
            0.2f to "Extracting text...",
            0.3f to "Detecting structure...",
            0.4f to "Identifying chapters...",
            0.5f to "Identifying topics...",
            0.6f to "Identifying subtopics...",
            0.7f to "Extracting facts and concepts...",
            0.8f to "Building knowledge map..."
        )
        
        for (stage in progressStages) {
            _analysisProgress.value = stage.first
            delay(400)
        }

        _analysisProgress.value = 0.9f
        // Phase 5: Question Bank Generation
        val bulkBank = mutableListOf<QuestionEntity>()
        units.forEach { unit ->
            bulkBank.addAll(generator.generate(unit.id, unit.content, types, 10)) // 30 questions per unit bank
        }
        
        _analysisProgress.value = 0.95f
        // Finalize
        db.quizDao().insertQuestions(bulkBank)
        _analysisProgress.value = 1.0f
        delay(500)
    }

    private suspend fun simulateProgress() {
        // Not used anymore as we have syncPhasedAnalysis
    }

    fun generateQuiz(unitIds: List<Long>, types: List<String>, countPerType: Int, difficulty: String? = null) {
        viewModelScope.launch {
            val existingQuestions = repository.getQuestionsForUnits(unitIds)
            
            // Filter by type and optionally difficulty
            val filteredQuestions = existingQuestions.filter { 
                types.contains(it.type) && (difficulty == null || it.difficulty == difficulty)
            }
            
            // Total questions requested: types.size * countPerType
            val targetTotal = types.size * countPerType
            
            if (filteredQuestions.size >= targetTotal) {
                _generatedQuestions.value = filteredQuestions.shuffled().take(targetTotal)
            } else {
                // If not enough of specific difficulty or types exist, generate more on demand
                val generator = QuestionGenerator()
                val additional = mutableListOf<QuestionEntity>()
                val unitsToProcess = _currentUnits.value.filter { it.id in unitIds }
                
                for (unit in unitsToProcess) {
                    additional.addAll(generator.generate(unit.id, unit.content, types, countPerType))
                }
                
                db.quizDao().insertQuestions(additional)
                // Filter the merged set
                val finalPool = (filteredQuestions + additional).filter { 
                    types.contains(it.type) && (difficulty == null || it.difficulty == difficulty)
                }
                _generatedQuestions.value = finalPool.shuffled().take(targetTotal.coerceAtMost(finalPool.size))
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
