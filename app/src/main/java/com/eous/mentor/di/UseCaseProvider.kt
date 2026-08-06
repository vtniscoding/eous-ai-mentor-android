package com.eous.mentor.di

import com.eous.mentor.domain.usecase.auth.*
import com.eous.mentor.domain.usecase.library.*
import com.eous.mentor.domain.usecase.bookmark.*
import com.eous.mentor.domain.usecase.friend.*
import com.eous.mentor.domain.usecase.chat.*
import com.eous.mentor.domain.usecase.progress.GetProgressStatsUseCase
import com.eous.mentor.domain.usecase.home.GetHomeStatsUseCase
import com.eous.mentor.domain.usecase.profile.*
import com.eous.mentor.domain.usecase.quiz.*
import com.eous.mentor.domain.usecase.session.*

/**
 * Service locator for use cases, mirroring RepositoryProvider.
 * Keeps ViewModels free of repository wiring.
 */
object UseCaseProvider {
    val getHomeStats by lazy {
        GetHomeStatsUseCase(
            RepositoryProvider.userRepository,
            RepositoryProvider.chatRepository,
            RepositoryProvider.sessionRepository
        )
    }

    // auth
    val login by lazy { LoginUseCase(RepositoryProvider.authRepository) }
    val register by lazy { RegisterUseCase(RepositoryProvider.authRepository) }
    val logout by lazy { LogoutUseCase(RepositoryProvider.authRepository) }

    // quiz
    val getQuizzes by lazy { GetQuizzesUseCase(RepositoryProvider.userRepository) }
    val saveQuizProgress by lazy { SaveQuizProgressUseCase(RepositoryProvider.userRepository) }
    val completeQuiz by lazy { CompleteQuizUseCase(RepositoryProvider.userRepository) }
    val resetQuiz by lazy { ResetQuizUseCase(RepositoryProvider.userRepository) }
    val generateQuiz by lazy { GenerateQuizUseCase(RepositoryProvider.userRepository, RepositoryProvider.chatRepository) }

    // bookmark
    val toggleBookmark by lazy { ToggleBookmarkUseCase(RepositoryProvider.chatRepository) }
    val toggleSessionBookmark by lazy { ToggleSessionBookmarkUseCase(RepositoryProvider.chatRepository) }

    // library
    val getLibraryContent by lazy { GetLibraryContentUseCase(RepositoryProvider.chatRepository, RepositoryProvider.userRepository) }

    // friend
    val getFriendsOverview by lazy { GetFriendsOverviewUseCase(RepositoryProvider.userRepository) }
    val getFriendProfile by lazy { GetFriendProfileUseCase(RepositoryProvider.userRepository) }
    val searchUsers by lazy { SearchUsersUseCase(RepositoryProvider.userRepository) }
    val sendFriendRequest by lazy { SendFriendRequestUseCase(RepositoryProvider.userRepository) }
    val acceptFriendRequest by lazy { AcceptFriendRequestUseCase(RepositoryProvider.userRepository) }
    val removeFriendship by lazy { RemoveFriendshipUseCase(RepositoryProvider.userRepository) }
    val getFriendsList by lazy { GetFriendsListUseCase(RepositoryProvider.userRepository) }

    // progress
    val getProgressStats by lazy { GetProgressStatsUseCase(RepositoryProvider.userRepository, RepositoryProvider.chatRepository) }

    // chat
    val getSessions by lazy { GetSessionsUseCase(RepositoryProvider.chatRepository) }
    val createSession by lazy { CreateSessionUseCase(RepositoryProvider.chatRepository) }
    val deleteSession by lazy { DeleteSessionUseCase(RepositoryProvider.chatRepository) }
    val deleteAllSessions by lazy { DeleteAllSessionsUseCase(RepositoryProvider.chatRepository) }
    val renameSession by lazy { RenameSessionUseCase(RepositoryProvider.chatRepository) }
    val getSessionMessages by lazy { GetSessionMessagesUseCase(RepositoryProvider.chatRepository) }
    val insertMessage by lazy { InsertMessageUseCase(RepositoryProvider.chatRepository) }
    val uploadChatImage by lazy { UploadChatImageUseCase(RepositoryProvider.chatRepository) }
    val requestAiReply by lazy { RequestAiReplyUseCase(RepositoryProvider.chatRepository, RepositoryProvider.userRepository) }

    // profile
    val getProfile by lazy { GetProfileUseCase(RepositoryProvider.userRepository) }
    val updateSubjects by lazy { UpdateSubjectsUseCase(RepositoryProvider.userRepository) }
    val updateEducationLevel by lazy { UpdateEducationLevelUseCase(RepositoryProvider.userRepository) }
    val updateExplanationStyle by lazy { UpdateExplanationStyleUseCase(RepositoryProvider.userRepository) }
    val uploadAvatar by lazy { UploadAvatarUseCase(RepositoryProvider.userRepository) }
    val deleteAvatar by lazy { DeleteAvatarUseCase(RepositoryProvider.userRepository) }
    val saveOnboardingProfile by lazy { SaveOnboardingProfileUseCase(RepositoryProvider.userRepository) }

    // session
    val issueLocalSession by lazy { IssueLocalSessionUseCase(RepositoryProvider.sessionRepository, RepositoryProvider.userRepository) }
    val isSessionTakenOver by lazy { IsSessionTakenOverUseCase(RepositoryProvider.sessionRepository) }
    val getRemoteSessionId by lazy { GetRemoteSessionIdUseCase(RepositoryProvider.userRepository) }
}
