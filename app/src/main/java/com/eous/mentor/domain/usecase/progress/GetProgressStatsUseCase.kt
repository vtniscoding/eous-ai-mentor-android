package com.eous.mentor.domain.usecase.progress

import com.eous.mentor.domain.model.DashboardStats
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class GetProgressStatsUseCase(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId: String, recordActivity: Boolean = true): Result<DashboardStats> = runCatching {
        if (userId.isEmpty()) throw IllegalArgumentException("User ID is empty")

        // 1. Fetch profile first to check name
        val profileResult = userRepository.getProfile(userId)
        val profile = profileResult.getOrNull()

        var currentDisplayName = "Student"
        if (profile != null) {
            val displayEmail = profile.email ?: "Student"
            currentDisplayName = profile.display_name ?: displayEmail.substringBefore("@")
        }

        // 2. Fetch remaining data in parallel
        coroutineScope {
            val sessionsDeferred = async { chatRepository.getSessions(userId).getOrDefault(emptyList()) }
            val messagesDeferred = async { chatRepository.getLegacyMessages(userId).getOrDefault(emptyList()) }
            val bookmarksDeferred = async { userRepository.getBookmarks(userId).getOrDefault(emptyList()) }
            val bookmarkedMessagesDeferred = async { chatRepository.getBookmarkedMessages(userId).getOrDefault(emptyList()) }
            val quizzesDeferred = async { userRepository.getQuizzes(userId).getOrDefault(emptyList()) }

            val sessions = sessionsDeferred.await()
            val messages = messagesDeferred.await()
            val bookmarks = bookmarksDeferred.await()
            val bookmarkedMessages = bookmarkedMessagesDeferred.await()
            val quizzes = quizzesDeferred.await()

            val sessionSubjectMap = sessions.filter { it.id != null }.associate { it.id!! to it.subject }

            val totalQueries = messages.count { it.role == "user" }
            val libraryItems = bookmarks.size

            val subjectCounts = mutableMapOf<String, Int>()

            fun recordSubject(rawSub: String?) {
                if (rawSub.isNullOrBlank() || rawSub.equals("General", ignoreCase = true)) return
                val trimmed = rawSub.trim().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
                }
                subjectCounts[trimmed] = (subjectCounts[trimmed] ?: 0) + 1
            }

            // Count subjects from AI-classified chat messages mapped to session subject
            messages.forEach { msg ->
                if (msg.role == "ai") {
                    val sessionSub = msg.session_id?.let { sessionSubjectMap[it] }
                    val finalSub = if (sessionSub.isNullOrBlank() || sessionSub.equals("General", ignoreCase = true)) {
                        msg.subject
                    } else {
                        sessionSub
                    }
                    recordSubject(finalSub)
                }
            }

            val totalSubjectItems = subjectCounts.values.sum()
            val computedSubjectStats = if (totalSubjectItems > 0) {
                subjectCounts.map { (name, count) ->
                    com.eous.mentor.domain.model.SubjectStat(
                        name = name,
                        percentage = Math.round((count.toFloat() / totalSubjectItems) * 100)
                    )
                }.sortedByDescending { it.percentage }
            } else {
                emptyList()
            }

            val mathPct = computedSubjectStats.find { it.name.equals("Math", ignoreCase = true) }?.percentage ?: 0
            val itPct = computedSubjectStats.find { it.name.equals("Programming", ignoreCase = true) }?.percentage ?: 0
            val sciencePct = computedSubjectStats.find { it.name.equals("Science", ignoreCase = true) }?.percentage ?: 0

            // 3. Record user daily activity and retrieve persistent DB streak
            val recordedProfile = if (recordActivity) userRepository.recordUserActivity(userId).getOrNull() else null
            val streak = recordedProfile?.current_streak ?: profile?.current_streak ?: 0

            val totalXp = totalQueries * 10 + libraryItems * 20
            if (recordActivity) {
                userRepository.updateUserXp(userId, totalXp)
            }
            val level = (totalXp / 100) + 1
            val xp = totalXp % 100

            val todayDate = java.time.LocalDate.now()
            val dayOfWeekVal = todayDate.dayOfWeek.value // 1 (Mon) to 7 (Sun)
            val startOfThisWeek = todayDate.minusDays((dayOfWeekVal - 1).toLong())
            val startOfLastWeek = startOfThisWeek.minusWeeks(1)

            val thisWeekDates = (0..6).map { startOfThisWeek.plusDays(it.toLong()) }
            val lastWeekDates = (0..6).map { startOfLastWeek.plusDays(it.toLong()) }

            val thisWeekActivity = thisWeekDates.map { date ->
                val dateStr = date.toString()
                messages.count { msg -> msg.role == "user" && msg.created_at?.startsWith(dateStr) == true }
            }

            val lastWeekActivity = lastWeekDates.map { date ->
                val dateStr = date.toString()
                messages.count { msg -> msg.role == "user" && msg.created_at?.startsWith(dateStr) == true }
            }

            val thisWeekDateLabels = thisWeekDates.map { "${it.dayOfMonth}/${it.monthValue}" }
            val lastWeekDateLabels = lastWeekDates.map { "${it.dayOfMonth}/${it.monthValue}" }

            val rawLevel = profile?.education_level ?: recordedProfile?.education_level
            val formattedEducationLevel = when (rawLevel) {
                "middle_school" -> "Middle School"
                "high_school" -> "High School"
                "university" -> "University / College"
                else -> "Not Set"
            }

            DashboardStats(
                displayName = currentDisplayName,
                totalQueries = totalQueries,
                libraryItems = libraryItems,
                streak = streak,
                studyTime = String.format(java.util.Locale.US, "%.1f", totalQueries * 0.15),
                level = level,
                xp = xp,
                mathPct = mathPct,
                itPct = itPct,
                sciencePct = sciencePct,
                subjectStats = computedSubjectStats,
                quizzes = quizzes,
                thisWeekActivity = thisWeekActivity,
                lastWeekActivity = lastWeekActivity,
                thisWeekDateLabels = thisWeekDateLabels,
                lastWeekDateLabels = lastWeekDateLabels,
                avatarUrl = recordedProfile?.avatar_url ?: profile?.avatar_url,
                educationLevel = formattedEducationLevel
            )
        }
    }
}
