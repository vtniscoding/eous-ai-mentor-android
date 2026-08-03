package com.eous.mentor.features.chat

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
            "tôi chỉ có thể hỗ trợ",
            "tôi là một trợ lý học tập",
            "không thuộc phạm vi học tập",
            "tôi không thể trả lời",
            "tôi không thể giúp bạn",
            "cannot assist with this request",
            "only assist with academic",
            "study-related queries",
            "không liên quan đến học tập"
        )
        val isRefusal = refusalKeywords.any { replyTrimmed.lowercase().contains(it) }
        if (isRefusal) {
            return ParsedAnswer(
                type = AnswerType.REFUSAL,
                explanation = replyTrimmed
            )
        }

        // 2. Extract Formula block (only extract and remove the first occurrence)
        var formula: String? = null
        var textWithoutFormula = replyTrimmed

        val doubleDollarRegex = Regex("""\$\$(.*?)\$\$""", RegexOption.DOT_MATCHES_ALL)
        val doubleMatch = doubleDollarRegex.find(replyTrimmed)
        if (doubleMatch != null) {
            formula = doubleMatch.groupValues[1].trim()
            textWithoutFormula = replyTrimmed.replaceFirst(doubleDollarRegex, "").trim()
        } else {
            val singleDollarRegex = Regex("""\$(.*?)\$""")
            val singleMatch = singleDollarRegex.find(replyTrimmed)
            if (singleMatch != null) {
                formula = singleMatch.groupValues[1].trim()
                textWithoutFormula = replyTrimmed.replaceFirst(singleDollarRegex, "").trim()
            }
        }

        // 3. Extract Conclusion
        var conclusion: String? = null
        var textWithoutConclusion = textWithoutFormula

        val conclusionKeywords = listOf(
            "Kết luận:",
            "Đáp số:",
            "Đáp án:",
            "Final conclusion:",
            "Conclusion:"
        )

        for (keyword in conclusionKeywords) {
            val index = textWithoutConclusion.indexOf(keyword, ignoreCase = true)
            if (index != -1) {
                conclusion = textWithoutConclusion.substring(index + keyword.length).trim()
                textWithoutConclusion = textWithoutConclusion.substring(0, index).trim()
                break
            }
        }

        // 4. Extract Steps (support multi-line content including formulas)
        val steps = mutableListOf<String>()
        val explanationLines = mutableListOf<String>()
        
        val lines = textWithoutConclusion.split("\n")
        val stepPattern = Regex("""^\s*(\d+)\.\s*(.*)""")
        
        var currentStepContent: StringBuilder? = null
        
        for (line in lines) {
            val match = stepPattern.matchEntire(line.trim())
            if (match != null) {
                if (currentStepContent != null) {
                    steps.add(currentStepContent.toString().trim())
                }
                val stepText = match.groupValues[2].trim()
                currentStepContent = StringBuilder(stepText)
            } else {
                if (currentStepContent != null) {
                    currentStepContent.append("\n").append(line)
                } else {
                    explanationLines.add(line)
                }
            }
        }
        
        if (currentStepContent != null) {
            steps.add(currentStepContent.toString().trim())
        }

        val explanation = explanationLines.joinToString("\n").trim()

        // 5. Determine Answer Type
        val type = if (formula != null || (steps.isNotEmpty() && conclusion != null)) {
            AnswerType.EXERCISE
        } else {
            AnswerType.CONCEPT
        }

        return ParsedAnswer(
            type = type,
            explanation = explanation.ifEmpty { if (steps.isEmpty() && formula == null) replyTrimmed else "" },
            formula = formula,
            steps = steps,
            conclusion = conclusion
        )
    }
}
