package com.eous.mentor.domain.usecase.home

import com.eous.mentor.domain.model.HomeData
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetHomeStatsUseCase(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val sessionRepository: com.eous.mentor.domain.repository.SessionRepository = com.eous.mentor.di.RepositoryProvider.sessionRepository
) {
    suspend operator fun invoke(userId: String): Result<HomeData> = coroutineScope {
        try {
            val messagesDeferred = async { chatRepository.getLegacyMessages(userId).getOrDefault(emptyList()) }
            val bookmarksDeferred = async { userRepository.getBookmarks(userId).getOrDefault(emptyList()) }
            val quizzesDeferred = async { userRepository.getQuizzes(userId).getOrDefault(emptyList()) }
            val profileDeferred = async { userRepository.getProfile(userId).getOrNull() }
            val recordedProfileDeferred = async { userRepository.recordUserActivity(userId).getOrNull() }

            val bookmarks = bookmarksDeferred.await()
            val quizzes = quizzesDeferred.await()
            val profile = profileDeferred.await()
            val recordedProfile = recordedProfileDeferred.await()
            val messages = messagesDeferred.await()

            val totalQueries = messages.count { it.role == "user" }
            val libraryItems = bookmarks.size
            val xp = (totalQueries * 10) + (libraryItems * 5)

            val fallbackName = sessionRepository.getCurrentUserEmail()
                ?.substringBefore("@")
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                ?: "Student"

            val currentDisplayName = recordedProfile?.display_name?.takeIf { it.isNotBlank() }
                ?: profile?.display_name?.takeIf { it.isNotBlank() }
                ?: fallbackName

            val streak = recordedProfile?.current_streak
                ?: profile?.current_streak
                ?: 0

            val rawLevel = profile?.education_level ?: recordedProfile?.education_level
            val formattedEducationLevel = when (rawLevel) {
                "middle_school" -> "Middle School"
                "high_school" -> "High School"
                "university" -> "University / College"
                else -> "Not Set"
            }

            Result.success(
                HomeData(
                    displayName = currentDisplayName,
                    avatarUrl = recordedProfile?.avatar_url ?: profile?.avatar_url,
                    educationLevel = formattedEducationLevel,
                    streak = streak,
                    xp = xp,
                    quizzes = quizzes
                )
            )
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
