package com.eous.mentor.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.ExperimentalSerializationApi

@Serializable
data class SavedAccount(
        val email: String,
        val password: String = "",
        val displayName: String = "",
        val avatarUrl: String? = null
)

@Serializable
data class UserContext(
        val education_level: String = "high_school",
        val explanation_style: String = "detailed",
        val subjects: List<String> = emptyList()
)

@Serializable data class TodoItem(val id: Int, val name: String)

data class SubjectStat(val name: String, val percentage: Int)

data class DashboardStats(
        val displayName: String,
        val totalQueries: Int,
        val libraryItems: Int,
        val streak: Int,
        val studyTime: String,
        val level: Int,
        val xp: Int,
        val mathPct: Int,
        val itPct: Int,
        val sciencePct: Int,
        val subjectStats: List<SubjectStat> = emptyList(),
        val quizzes: List<Quiz> = emptyList(),
        val thisWeekActivity: List<Int> = emptyList(),
        val lastWeekActivity: List<Int> = emptyList(),
        val thisWeekDateLabels: List<String> = emptyList(),
        val lastWeekDateLabels: List<String> = emptyList(),
        val avatarUrl: String? = null,
        val educationLevel: String = "Not Set"
)

@Serializable
data class HomeData(
        val displayName: String = "Student",
        val avatarUrl: String? = null,
        val educationLevel: String = "Not Set",
        val streak: Int = 0,
        val xp: Int = 0,
        val quizzes: List<Quiz> = emptyList(),
        val isStreak3Achieved: Boolean = false,
        val isStreakLost: Boolean = false,
        val pendingRequests: List<FriendshipWithProfile> = emptyList()
)

@Serializable
data class Profile(
        val id: String,
        val email: String? = null,
        val display_name: String? = null,
        val education_level: String? = "high_school",
        val explanation_style: String? = "detailed",
        val onboarding_completed: Boolean = false,
        val subjects: List<String> = emptyList(),
        val current_streak: Int = 0,
        val longest_streak: Int = 0,
        val last_active_date: String? = null,
        val avatar_url: String? = null,
        val current_session_id: String? = null,
        val xp: Int = 0
)

/** A chat session grouping messages together. Maps to the `sessions` table in Supabase. */
@Serializable
data class ChatSession(
        val id: String? = null,
        val user_id: String? = null,
        val title: String = "New Chat",
        val subject: String = "General",
        val created_at: String? = null
)

/** A single chat message (user or AI). Maps to the `messages` table in Supabase. */
@Serializable
data class ChatMessage(
        val id: String? = null,
        val user_id: String? = null,
        val session_id: String? = null,
        val role: String, // "user" or "ai"
        val content: String = "",
        val image: String? = null, // Storage URL for uploaded images
        val is_bookmarked: Boolean = false,
        val bookmark_folder: String? = null,
        val subject: String? = null,
        val quiz_id: String? = null, // Linked quiz ID when AI generates a quiz
        val review_status: String? = "pending",
        val created_at: String? = null
)

/**
 * Legacy Message used by existing dashboard stats fetching. Kept for backward compatibility with
 * totalQueries count.
 */
@Serializable
data class Message(
        val id: String? = null,
        val role: String,
        val content: String = "",
        val subject: String? = null,
        val created_at: String? = null,
        val session_id: String? = null
)

@Serializable
data class Bookmark(
        val id: String? = null,
        val user_id: String? = null,
        val message_id: String? = null,
        val folder: String? = null,
        val created_at: String? = null
)

@Serializable
data class Friendship(
        val id: String? = null,
        val sender_id: String,
        val receiver_id: String,
        val status: String,
        val created_at: String? = null,
        val updated_at: String? = null
)

@Serializable
data class FriendshipWithProfile(
        val id: String? = null,
        val sender_id: String,
        val receiver_id: String,
        val status: String,
        val created_at: String? = null,
        val updated_at: String? = null,
        val sender: Profile? = null,
        val receiver: Profile? = null
)

@Serializable
data class BookmarkWithMessage(
        val id: String? = null,
        val user_id: String? = null,
        val message_id: String? = null,
        val folder: String? = null,
        val created_at: String? = null,
        val message: ChatMessage? = null
)

@Serializable
data class Quiz(
        val id: String = "",
        val user_id: String? = null,
        val topic: String = "",
        val title: String = "Untitled Quiz",
        val questions: List<QuizQuestion> = emptyList(),
        val score: Int? = null,
        val total_questions: Int = 0,
        val current_question_index: Int = 0,
        val status: String = "not_started",
        val difficulty: String = "high_school",
        val created_at: String = ""
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class QuizQuestion(
        val id: Int = 0,
        val question: String = "",
        val options: List<String> = emptyList(),
        @JsonNames("correct_answer_index", "correctOptionIndex", "correctAnswerIndex")
        val correctAnswerIndex: Int = 0,
        val selectedAnswerIndex: Int? = null,
        val explanation: String? = null
)

/** Request body sent to the ai-chat Edge Function. */
@Serializable
data class AiChatRequest(
        val message: String,
        val history: List<AiChatHistoryItem> = emptyList(),
        @SerialName("image_url") val imageUrl: String? = null,
        @SerialName("user_context") val userContext: UserContext? = null
)

@Serializable
data class AiChatHistoryItem(
        val role: String, // "user" or "model"
        val content: String
)

/** Response body from the ai-chat Edge Function. */
@Serializable
data class AiChatResponse(
        val reply: String,
        val subject: String? = null,
        val quiz: Quiz? = null,
        val error: String? = null
)
