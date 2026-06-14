package com.example.data.local.entities

import androidx.room.*

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val rawText: String,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "units",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class UnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val title: String,
    val content: String,
    val orderIndex: Int
)

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("unitId")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val type: String, // MCQ, TRUE_FALSE, FILL_BLANKS
    val questionText: String,
    val correctAnswer: String,
    val distractors: String?, // Pipe separated for MCQ
    val explanation: String,
    val difficulty: String = "Medium" // Easy, Medium, Hard
)

@Entity(
    tableName = "quiz_sessions",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val score: Int,
    val totalQuestions: Int,
    val dateTaken: Long = System.currentTimeMillis(),
    val detailsJson: String // Detailed report: List of QuestionResult(questionId, userAnswer, isCorrect)
)
