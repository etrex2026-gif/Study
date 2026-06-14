package com.example.data.local

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM documents ORDER BY dateAdded DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Query("SELECT * FROM units WHERE documentId = :documentId ORDER BY orderIndex ASC")
    fun getUnitsForDocument(documentId: Long): Flow<List<UnitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Query("SELECT * FROM questions WHERE unitId IN (:unitIds)")
    suspend fun getQuestionsForUnits(unitIds: List<Long>): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizSession(session: QuizSessionEntity): Long

    @Query("SELECT * FROM quiz_sessions ORDER BY dateTaken DESC")
    fun getAllSessions(): Flow<List<QuizSessionEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: Long)
}

@Database(
    entities = [
        DocumentEntity::class,
        UnitEntity::class,
        QuestionEntity::class,
        QuizSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
}
