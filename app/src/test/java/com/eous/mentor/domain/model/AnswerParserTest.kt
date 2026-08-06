package com.eous.mentor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerParserTest {

    @Test
    fun `returns REFUSAL when reply contains cannot assist with this request`() {
        val reply = "Hello! I cannot assist with this request because it violates safety guidelines."
        val result = AnswerParser.parse(reply, "Math")
        assertEquals(AnswerType.REFUSAL, result.type)
        assertEquals(reply.trim(), result.explanation)
    }

    @Test
    fun `returns REFUSAL when reply contains only assist with academic`() {
        val reply = "Sorry, I can only assist with academic topics."
        val result = AnswerParser.parse(reply, "Physics")
        assertEquals(AnswerType.REFUSAL, result.type)
        assertEquals(reply.trim(), result.explanation)
    }

    @Test
    fun `returns REFUSAL when reply contains study-related queries`() {
        val reply = "I can only help with study-related queries."
        val result = AnswerParser.parse(reply, "Chemistry")
        assertEquals(AnswerType.REFUSAL, result.type)
        assertEquals(reply.trim(), result.explanation)
    }

    @Test
    fun `returns REFUSAL case-insensitively and with whitespace`() {
        val reply = "  I CANNOT ASSIST WITH THIS REQUEST...  "
        val result = AnswerParser.parse(reply, null)
        assertEquals(AnswerType.REFUSAL, result.type)
        assertEquals(reply.trim(), result.explanation)
    }

    @Test
    fun `returns EXERCISE when reply is normal answer`() {
        val reply = "The solution to 2+2 is 4."
        val result = AnswerParser.parse(reply, "Math")
        assertEquals(AnswerType.EXERCISE, result.type)
        assertEquals(reply.trim(), result.explanation)
    }
}
