package com.eous.mentor.core.util

import com.eous.mentor.domain.model.Quiz
import kotlinx.serialization.json.Json

/**
 * Client-side fallback parser for quizzes embedded in an AI reply.
 *
 * The `ai-chat` Edge Function already tries to extract the quiz server-side, but when the model
 * deviates from the expected format the quiz can arrive inlined in the reply text instead. This
 * object re-runs the same extraction patterns on the client so the quiz is not lost.
 */
object QuizParser {

    private val json = Json { ignoreUnknownKeys = true }

    private val tagRegex =
            Regex("""\[QuizJSON]\s*([\s\S]*?)\s*\[/QuizJSON]""", RegexOption.IGNORE_CASE)
    private val codeBlockRegex = Regex("""```(?:json)?\s*\n?([\s\S]*?)\n?\s*```""")
    private val rawJsonRegex = Regex("""\{[\s\S]*?"questions"\s*:\s*\[[\s\S]*?]\s*}""")

    /**
     * Attempts to extract a quiz from [reply].
     *
     * Tries, in order: `[QuizJSON]` tags, ```` ```json ```` code blocks, then a raw JSON object
     * containing a `questions` array.
     *
     * @return the parsed [Quiz] paired with the reply text stripped of the quiz payload, or `null`
     * when no valid quiz is present.
     */
    fun extractFromReply(reply: String): Pair<Quiz, String>? {
        tagRegex.find(reply)?.let { match ->
            decode(match.groupValues[1])?.let { return it to strip(reply, match.value) }
        }

        for (match in codeBlockRegex.findAll(reply)) {
            decode(match.groupValues[1])?.let { return it to strip(reply, match.value) }
        }

        rawJsonRegex.find(reply)?.let { match ->
            decode(match.value)?.let { return it to strip(reply, match.value) }
        }

        return null
    }

    private fun decode(candidate: String): Quiz? =
            try {
                json.decodeFromString<Quiz>(candidate.trim()).takeIf { it.questions.isNotEmpty() }
            } catch (_: Exception) {
                null
            }

    private fun strip(reply: String, payload: String): String = reply.replace(payload, "").trim()
}
