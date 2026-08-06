package com.eous.mentor.testutil

import com.eous.mentor.domain.model.*
import com.eous.mentor.domain.repository.UserRepository

/**
 * Fake implementation of [UserRepository] for unit testing.
 * Only stubs methods actually used in the test scope; others throw.
 */
class FakeUserRepository : UserRepository {

    var getRemoteSessionIdResult: Result<String?> = Result.success(null)
    var getRemoteSessionIdCallCount = 0; private set

    var updateSessionIdResult: Result<Unit> = Result.success(Unit)
    var updateSessionIdCallCount = 0; private set
    var lastUpdatedSessionId: String? = null; private set

    var getFriendsListResult: Result<List<Profile>> = Result.success(emptyList())

    var getProfileResult: Result<Profile?> = Result.success(null)
    var getBookmarksResult: Result<List<Bookmark>> = Result.success(emptyList())
    var getQuizzesResult: Result<List<Quiz>> = Result.success(emptyList())
    var recordUserActivityResult: Result<Profile?> = Result.success(null)
    var getPendingRequestsResult: Result<List<FriendshipWithProfile>> = Result.success(emptyList())
    var getSuggestedUsersResult: Result<List<Profile>> = Result.success(emptyList())
    var updateUserXpResult: Result<Unit> = Result.success(Unit)
    var lastUpdatedXp: Int? = null; private set
    var updateUserXpCallCount = 0; private set

    var saveOnboardingProfileResult: Result<Unit> = Result.success(Unit)
    var saveOnboardingProfileCallCount = 0; private set
    var lastOnboardingUserId: String? = null; private set
    var lastOnboardingEducationLevel: String? = null; private set
    var lastOnboardingExplanationStyle: String? = null; private set
    var lastOnboardingSubjects: List<String>? = null; private set

    var createQuizResult: Result<Quiz> = Result.failure(NotImplementedError())
    var createQuizCallCount = 0; private set
    var lastCreatedQuizTopic: String? = null; private set
    var lastCreatedQuizQuestions: List<QuizQuestion>? = null; private set

    override suspend fun getRemoteSessionId(userId: String): Result<String?> {
        getRemoteSessionIdCallCount++
        val res = getRemoteSessionIdResult
        return if (res.isSuccess) Result.success(res.getOrNull()) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun updateSessionId(userId: String, sessionId: String?): Result<Unit> {
        updateSessionIdCallCount++
        lastUpdatedSessionId = sessionId
        val res = updateSessionIdResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun getFriendsList(userId: String): Result<List<Profile>> {
        val res = getFriendsListResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun createQuiz(
        userId: String,
        topic: String,
        title: String,
        totalQuestions: Int,
        questions: List<QuizQuestion>,
        difficulty: String
    ): Result<Quiz> {
        createQuizCallCount++
        lastCreatedQuizTopic = topic
        lastCreatedQuizQuestions = questions
        val res = createQuizResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun getProfile(userId: String): Result<Profile?> {
        val res = getProfileResult
        return if (res.isSuccess) Result.success(res.getOrNull()) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun getBookmarks(userId: String): Result<List<Bookmark>> {
        val res = getBookmarksResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun getQuizzes(userId: String): Result<List<Quiz>> {
        val res = getQuizzesResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun recordUserActivity(userId: String): Result<Profile?> {
        val res = recordUserActivityResult
        return if (res.isSuccess) Result.success(res.getOrNull()) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun getPendingRequests(userId: String): Result<List<FriendshipWithProfile>> {
        val res = getPendingRequestsResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun getSuggestedUsers(userId: String, limit: Int): Result<List<Profile>> {
        val res = getSuggestedUsersResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun saveOnboardingProfile(
        userId: String,
        educationLevel: String,
        explanationStyle: String,
        subjects: List<String>
    ): Result<Unit> {
        saveOnboardingProfileCallCount++
        lastOnboardingUserId = userId
        lastOnboardingEducationLevel = educationLevel
        lastOnboardingExplanationStyle = explanationStyle
        lastOnboardingSubjects = subjects
        val res = saveOnboardingProfileResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun updateUserXp(userId: String, xp: Int): Result<Unit> {
        updateUserXpCallCount++
        lastUpdatedXp = xp
        val res = updateUserXpResult
        return if (res.isSuccess) Result.success(res.getOrNull()!!) else Result.failure(res.exceptionOrNull()!!)
    }

    override suspend fun updateQuiz(quizId: String, currentQuestionIndex: Int, score: Int?, status: String): Result<Unit> = Result.success(Unit)
    override suspend fun updateSubjects(userId: String, subjects: List<String>): Result<Unit> = Result.success(Unit)
    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String> = Result.success("")
    override suspend fun deleteAvatar(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun updateExplanationStyle(userId: String, style: String): Result<Unit> = Result.success(Unit)
    override suspend fun updateEducationLevel(userId: String, level: String): Result<Unit> = Result.success(Unit)
    override suspend fun searchUsers(query: String): Result<List<Profile>> = Result.success(emptyList())
    override suspend fun sendFriendRequest(senderId: String, receiverId: String): Result<Unit> = Result.success(Unit)
    override suspend fun acceptFriendRequest(senderId: String, receiverId: String): Result<Unit> = Result.success(Unit)
    override suspend fun declineOrRemoveFriendship(senderId: String, receiverId: String): Result<Unit> = Result.success(Unit)
}
