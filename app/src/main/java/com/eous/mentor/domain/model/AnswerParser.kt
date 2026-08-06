package com.eous.mentor.domain.model

enum class AnswerType {
    REFUSAL,
    CONCEPT,
    EXERCISE
}

data class ParsedAnswer(
    val type: AnswerType,
    val explanation: String,
    val formula: String? = null,
    val steps: List<String> = emptyList(),
    val conclusion: String? = null
)

object AnswerParser {
    fun parse(reply: String, subject: String?): ParsedAnswer {
        val replyTrimmed = reply.trim()

        // 1. Detect Refusal
        val refusalKeywords = listOf(
            "cannot assist with this request",
            "only assist with academic",
            "study-related queries"
        )
        val isRefusal = refusalKeywords.any { replyTrimmed.lowercase().contains(it) }
        if (isRefusal) {
            return ParsedAnswer(
                type = AnswerType.REFUSAL,
                explanation = replyTrimmed
            )
        }

        // Return full explanation intact to prevent slicing text and formulas
        return ParsedAnswer(
            type = AnswerType.EXERCISE,
            explanation = replyTrimmed
        )
    }
}

