package com.example.domain.analyzer

import com.example.data.local.entities.UnitEntity

class ContentAnalyzer {

    fun analyze(documentId: Long, rawText: String): List<UnitEntity> {
        val lines = rawText.lines()
        val units = mutableListOf<UnitEntity>()
        var currentTitle = "General Introduction"
        var currentContent = StringBuilder()
        var orderIndex = 0

        // Heuristics for titles: 
        // 1. Matches "Chapter/Unit/Section X"
        // 2. All CAPS and short
        // 3. Numbered titles like "1.1 Introduction"
        val explicitHeaderRegex = Regex("""^(Chapter|Unit|Section|Part|Topic)\s+[A-Z0-9]+.*|^\d+(\.\d+)*\s+[A-Z].*""", RegexOption.IGNORE_CASE)

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val isStructuralHeader = explicitHeaderRegex.matches(trimmed)
            val isUpperCaseHeader = trimmed.length in 4..60 && trimmed.all { it.isUpperCase() || !it.isLetter() }
            
            if (isStructuralHeader || isUpperCaseHeader) {
                // Save previous unit if it has substance
                if (currentContent.trim().length > 50) {
                    units.add(UnitEntity(
                        documentId = documentId,
                        title = currentTitle,
                        content = currentContent.toString().trim(),
                        orderIndex = orderIndex++
                    ))
                    currentContent = StringBuilder()
                }
                currentTitle = trimmed
            } else {
                currentContent.append(line).append("\n")
            }
        }

        // Add last unit
        if (currentContent.trim().isNotBlank()) {
            units.add(UnitEntity(
                documentId = documentId,
                title = currentTitle,
                content = currentContent.toString().trim(),
                orderIndex = orderIndex++
            ))
        }

        // Final fallback: If text was too flat, split by size roughly
        if (units.isEmpty() && rawText.isNotBlank()) {
            units.add(UnitEntity(
                documentId = documentId,
                title = "Study Material",
                content = rawText.trim(),
                orderIndex = 0
            ))
        }

        return units
    }
}
