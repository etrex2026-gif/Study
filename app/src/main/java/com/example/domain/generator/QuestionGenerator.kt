package com.example.domain.generator

import com.example.data.local.entities.QuestionEntity
import kotlin.random.Random

class QuestionGenerator {

    fun generate(
        unitId: Long,
        content: String,
        types: List<String>,
        questionCountPerType: Int
    ): List<QuestionEntity> {
        // Split content into sentences and filter for fact-dense ones
        val sentences = content.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.length in 40..400 } 
            .distinct()
            .shuffled()

        val questions = mutableListOf<QuestionEntity>()
        val usedSentences = mutableSetOf<String>()

        for (type in types) {
            var generatedCount = 0
            for (sentence in sentences) {
                if (generatedCount >= questionCountPerType) break
                if (sentence in usedSentences) continue

                val question = when (type) {
                    "MCQ" -> generateMCQ(unitId, sentence, sentences)
                    "TRUE_FALSE" -> generateTF(unitId, sentence)
                    "FILL_BLANKS" -> generateFillBlanks(unitId, sentence)
                    else -> null
                }

                if (question != null) {
                    val difficulty = when {
                        sentence.length > 250 || sentence.contains(Regex("(however|consequently|whereas|nevertheless)", RegexOption.IGNORE_CASE)) -> "Hard"
                        sentence.length > 130 || sentence.contains(Regex("(because|since|therefore|resulting)", RegexOption.IGNORE_CASE)) -> "Medium"
                        else -> "Easy"
                    }
                    questions.add(question.copy(difficulty = difficulty))
                    usedSentences.add(sentence)
                    generatedCount++
                }
            }
        }
        return questions.shuffled()
    }

    private fun generateMCQ(unitId: Long, fact: String, allSentences: List<String>): QuestionEntity? {
        val keywords = extractKeywords(fact)
        if (keywords.isEmpty()) return null
        
        val target = keywords.random()
        val questionText = "According to the text, which term best completes this statement?\n\n\"${fact.replace(target, "__________", ignoreCase = true)}\""

        // Find believable distractors (other keywords from the text)
        val distractors = allSentences
            .flatMap { extractKeywords(it) }
            .filter { it.lowercase() != target.lowercase() && it.length in (target.length - 3)..(target.length + 3) }
            .distinctBy { it.lowercase() }
            .shuffled()
            .take(3)

        if (distractors.size < 2) return null

        return QuestionEntity(
            unitId = unitId,
            type = "MCQ",
            questionText = questionText,
            correctAnswer = target,
            distractors = distractors.joinToString("|"),
            explanation = "Direct reference from text: \"$fact\""
        )
    }

    private fun generateTF(unitId: Long, fact: String): QuestionEntity {
        val isTrue = Random.nextBoolean()
        var questionText = fact
        var correctAnswer = "True"
        
        if (!isTrue) {
            val keywords = extractKeywords(fact)
            if (keywords.isNotEmpty()) {
                val target = keywords.random()
                // Simple negation or term swap
                questionText = fact.replace(target, "[Inconsistent Concept]", ignoreCase = true)
                correctAnswer = "False"
            } else {
                questionText = "The following is NOT found in the text: $fact"
                correctAnswer = "False"
            }
        }

        return QuestionEntity(
            unitId = unitId,
            type = "TRUE_FALSE",
            questionText = "Is the following statement technically accurate according to the provided material?\n\n\"$questionText\"",
            correctAnswer = correctAnswer,
            distractors = if (correctAnswer == "True") "False" else "True",
            explanation = "Fact from source: \"$fact\""
        )
    }

    private fun generateFillBlanks(unitId: Long, fact: String): QuestionEntity? {
        val keywords = extractKeywords(fact)
        if (keywords.isEmpty()) return null
        val target = keywords.shuffled().first()
        
        return QuestionEntity(
            unitId = unitId,
            type = "FILL_BLANKS",
            questionText = "Complete the passage with the correct term:\n\n\"${fact.replace(target, "__________", ignoreCase = true)}\"",
            correctAnswer = target,
            distractors = null,
            explanation = "Full statement: \"$fact\""
        )
    }

    private fun extractKeywords(text: String): List<String> {
        val stopWords = setOf("the", "and", "this", "that", "from", "with", "which", "their", "these", "those")
        return text.split(Regex("[^a-zA-Z]"))
            .filter { it.length > 5 && !stopWords.contains(it.lowercase()) }
            .distinct()
    }
}
