package com.eous.mentor.data.repository

import com.eous.mentor.di.supabase
import com.eous.mentor.domain.model.Bookmark
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.model.QuizQuestion
import com.eous.mentor.domain.repository.UserRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable

@Serializable
private data class QuizInsertDto(
        val user_id: String,
        val topic: String,
        val title: String,
        val total_questions: Int,
        val status: String = "not_started",
        val current_question_index: Int = 0,
        val questions: List<QuizQuestion>,
        val difficulty: String = "medium"
)

class UserRepositoryImpl : UserRepository {
    override suspend fun getProfile(userId: String): Result<Profile?> {
        return try {
            val profile =
                    supabase.from("profiles")
                            .select { filter { eq("id", userId) } }
                            .decodeSingleOrNull<Profile>()
            Result.success(profile)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun getBookmarks(userId: String): Result<List<Bookmark>> {
        return try {
            val bookmarks = supabase.from("bookmarks").select().decodeList<Bookmark>()
            Result.success(bookmarks)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun getQuizzes(userId: String): Result<List<Quiz>> {
        return try {
            val quizzes =
                    supabase.from("quizzes")
                            .select { filter { eq("user_id", userId) } }
                            .decodeList<Quiz>()
            Result.success(quizzes)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun createQuiz(
            userId: String,
            topic: String,
            title: String,
            totalQuestions: Int,
            questions: List<QuizQuestion>,
            difficulty: String
    ): Result<Quiz> {
        return try {
            val newQuiz =
                    supabase.from("quizzes")
                            .insert(
                                    QuizInsertDto(
                                            user_id = userId,
                                            topic = topic,
                                            title = title,
                                            total_questions = totalQuestions,
                                            status = "not_started",
                                            current_question_index = 0,
                                            questions = questions,
                                            difficulty = difficulty
                                    )
                            ) { select() }
                            .decodeSingle<Quiz>()
            Result.success(newQuiz)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateQuiz(
            quizId: String,
            currentQuestionIndex: Int,
            score: Int?,
            status: String
    ): Result<Unit> {
        return try {
            supabase.from("quizzes").update({
                        set("current_question_index", currentQuestionIndex)
                        if (score != null) {
                            set("score", score)
                        }
                        set("status", status)
                    }) { filter { eq("id", quizId) } }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateSubjects(userId: String, subjects: List<String>): Result<Unit> {
        return try {
            supabase.from("profiles").update({ set("subjects", subjects) }) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun recordUserActivity(userId: String): Result<Profile?> {
        return try {
            val currentProfile = getProfile(userId).getOrNull()
            val todayStr = java.time.LocalDate.now().toString()
            val yesterdayStr = java.time.LocalDate.now().minusDays(1).toString()

            val lastActive = currentProfile?.last_active_date
            var newStreak = currentProfile?.current_streak ?: 0
            var newLongest = currentProfile?.longest_streak ?: 0

            if (lastActive == todayStr) {
                // Already active today, return current profile without extra DB writes
                return Result.success(currentProfile)
            } else if (lastActive == yesterdayStr) {
                // Consecutive active day! Increment streak
                newStreak += 1
            } else {
                // Missed one or more days, reset streak to 1
                newStreak = 1
            }

            if (newStreak > newLongest) {
                newLongest = newStreak
            }

            supabase.from("profiles").update({
                        set("current_streak", newStreak)
                        set("longest_streak", newLongest)
                        set("last_active_date", todayStr)
                    }) { filter { eq("id", userId) } }

            val updatedProfile = getProfile(userId).getOrNull()
            Result.success(updatedProfile)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateExplanationStyle(userId: String, style: String): Result<Unit> {
        return try {
            supabase.from("profiles").update({ set("explanation_style", style) }) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateEducationLevel(userId: String, level: String): Result<Unit> {
        return try {
            supabase.from("profiles").update({
                set("education_level", level)
            }) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun saveOnboardingProfile(
        userId: String,
        educationLevel: String,
        explanationStyle: String,
        subjects: List<String>
    ): Result<Unit> {
        return try {
            supabase.from("profiles").update({
                set("education_level", educationLevel)
                set("explanation_style", explanationStyle)
                set("subjects", subjects)
                set("onboarding_completed", true)
            }) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String> {
        return try {
            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            val path = "$userId/$fileName"
            val bucket = supabase.storage.from("avatars")
            bucket.upload(path, imageBytes) { upsert = true }
            val publicUrl = bucket.publicUrl(path)

            supabase.from("profiles").update({ set("avatar_url", publicUrl) }) {
                filter { eq("id", userId) }
            }

            Result.success(publicUrl)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteAvatar(userId: String): Result<Unit> {
        return try {
            supabase.from("profiles").update({ set("avatar_url", null as String?) }) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
