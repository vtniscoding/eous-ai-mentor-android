package com.eous.mentor.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class QuizParserTest {

    private val sampleQuizJson = """
        {
          "topic": "Math",
          "title": "Addition quiz",
          "questions": [
            {
              "id": 1,
              "question": "What is 1+1?",
              "options": ["1", "2", "3"],
              "correctAnswerIndex": 1
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `extracts quiz successfully from QuizJSON tags`() {
        val reply = "Here is a quiz:\n[QuizJSON]\n$sampleQuizJson\n[/QuizJSON]\nGood luck!"
        val result = QuizParser.extractFromReply(reply)

        assertNotNull(result)
        val (quiz, strippedReply) = result!!
        assertEquals("Math", quiz.topic)
        assertEquals("Addition quiz", quiz.title)
        assertEquals(1, quiz.questions.size)
        assertEquals("What is 1+1?", quiz.questions[0].question)
        assertEquals("Here is a quiz:\n\nGood luck!", strippedReply)
    }

    @Test
    fun `extracts quiz successfully from markdown code blocks`() {
        val reply = "Here is a quiz:\n```json\n$sampleQuizJson\n```\nGood luck!"
        val result = QuizParser.extractFromReply(reply)

        assertNotNull(result)
        val (quiz, strippedReply) = result!!
        assertEquals("Math", quiz.topic)
        assertEquals("Here is a quiz:\n\nGood luck!", strippedReply)
    }

    @Test
    fun `extracts quiz successfully from raw JSON block`() {
        val reply = "Some text before $sampleQuizJson and text after"
        val result = QuizParser.extractFromReply(reply)

        assertNotNull(result)
        val (quiz, strippedReply) = result!!
        assertEquals("Math", quiz.topic)
        assertEquals("Some text before  and text after", strippedReply)
    }

    @Test
    fun `returns null when reply contains no quiz`() {
        val reply = "This is a normal chat reply with no JSON"
        val result = QuizParser.extractFromReply(reply)
        assertNull(result)
    }

    @Test
    fun `returns null when JSON is invalid`() {
        val reply = "[QuizJSON]\n{invalid: json}\n[/QuizJSON]"
        val result = QuizParser.extractFromReply(reply)
        assertNull(result)
    }

    @Test
    fun `returns null when quiz has no questions`() {
        val emptyQuizJson = """
            {
              "topic": "Math",
              "title": "Empty quiz",
              "questions": []
            }
        """.trimIndent()
        val reply = "[QuizJSON]\n$emptyQuizJson\n[/QuizJSON]"
        val result = QuizParser.extractFromReply(reply)
        assertNull(result)
    }
}
