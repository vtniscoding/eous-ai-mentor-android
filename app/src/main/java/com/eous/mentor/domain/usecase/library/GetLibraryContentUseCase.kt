package com.eous.mentor.domain.usecase.library

import com.eous.mentor.domain.model.LibraryContent
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GetLibraryContentUseCase(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<LibraryContent> = coroutineScope {
        runCatching {
            val bookmarksDeferred = async { chatRepository.getBookmarkedMessages(userId).getOrDefault(emptyList()) }
            val quizzesDeferred = async { userRepository.getQuizzes(userId).getOrDefault(emptyList()) }
            val sessionsDeferred = async { chatRepository.getSessions(userId).getOrThrow() }

            val bookmarks = bookmarksDeferred.await()
            val quizzes = quizzesDeferred.await()
            val sessionsList = sessionsDeferred.await()

            val hasPracticedToday = quizzes.any { quiz ->
                val time = parseCreatedAt(quiz.created_at) ?: 0L
                isToday(time)
            }

            val uniqueSubjects = sessionsList
                .map { it.subject }
                .filter { it.isNotBlank() }
                .distinct()

            val now = System.currentTimeMillis()
            val oneWeekAgo = now - 7 * 24 * 60 * 60 * 1000L
            val weeklySessions = sessionsList.filter { session ->
                val time = parseCreatedAt(session.created_at) ?: 0L
                time >= oneWeekAgo
            }
            val subjectCounts = weeklySessions.groupBy { it.subject }.mapValues { it.value.size }
            val mostFrequentSubject = subjectCounts.filterKeys { it.isNotBlank() }.maxByOrNull { it.value }
            val practiceSubject = mostFrequentSubject?.key ?: "Math"
            val practiceQuestionCount = mostFrequentSubject?.value ?: 0

            LibraryContent(
                bookmarkedMessages = bookmarks,
                sessions = sessionsList,
                subjects = uniqueSubjects,
                practiceSubject = practiceSubject,
                practiceQuestionCount = practiceQuestionCount,
                hasPracticedToday = hasPracticedToday
            )
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val todayStr = sdf.format(Date())
        val dateStr = sdf.format(Date(timestamp))
        return todayStr == dateStr
    }

    private fun parseCreatedAt(createdAt: String?): Long? {
        if (createdAt.isNullOrEmpty()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(createdAt.take(19))?.time
        } catch (e: Exception) {
            null
        }
    }
}
