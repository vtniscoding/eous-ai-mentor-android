package com.eous.mentor.features.chat

import com.eous.mentor.domain.model.*
import com.eous.mentor.domain.usecase.chat.*
import com.eous.mentor.domain.usecase.bookmark.ToggleBookmarkUseCase
import com.eous.mentor.domain.usecase.profile.GetProfileUseCase
import com.eous.mentor.domain.usecase.profile.UpdateExplanationStyleUseCase
import com.eous.mentor.testutil.FakeChatRepository
import com.eous.mentor.testutil.FakeUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeChatRepo = FakeChatRepository()
    private val fakeUserRepo = FakeUserRepository()

    // Instantiate real use cases with fake repositories
    private val getProfileUseCase = GetProfileUseCase(fakeUserRepo)
    private val getSessionsUseCase = GetSessionsUseCase(fakeChatRepo)
    private val createSessionUseCase = CreateSessionUseCase(fakeChatRepo)
    private val deleteSessionUseCase = DeleteSessionUseCase(fakeChatRepo)
    private val deleteAllSessionsUseCase = DeleteAllSessionsUseCase(fakeChatRepo)
    private val renameSessionUseCase = RenameSessionUseCase(fakeChatRepo)
    private val getSessionMessagesUseCase = GetSessionMessagesUseCase(fakeChatRepo)
    private val insertMessageUseCase = InsertMessageUseCase(fakeChatRepo)
    private val requestAiReplyUseCase = RequestAiReplyUseCase(fakeChatRepo, fakeUserRepo)
    private val toggleBookmarkUseCase = ToggleBookmarkUseCase(fakeChatRepo)
    private val uploadChatImageUseCase = UploadChatImageUseCase(fakeChatRepo)
    private val updateExplanationStyleUseCase = UpdateExplanationStyleUseCase(fakeUserRepo)

    private val userId = "user-123"

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Set initial fake results
        fakeUserRepo.getProfileResult = Result.success(Profile(id = userId, display_name = "Alice"))
        fakeChatRepo.getSessionsResult = Result.success(emptyList())

        viewModel = ChatViewModel(
            userId = userId,
            initialQuestion = "",
            getProfileUseCase = getProfileUseCase,
            getSessionsUseCase = getSessionsUseCase,
            createSessionUseCase = createSessionUseCase,
            deleteSessionUseCase = deleteSessionUseCase,
            deleteAllSessionsUseCase = deleteAllSessionsUseCase,
            renameSessionUseCase = renameSessionUseCase,
            getSessionMessagesUseCase = getSessionMessagesUseCase,
            insertMessageUseCase = insertMessageUseCase,
            requestAiReplyUseCase = requestAiReplyUseCase,
            toggleBookmarkUseCase = toggleBookmarkUseCase,
            uploadChatImageUseCase = uploadChatImageUseCase,
            updateExplanationStyleUseCase = updateExplanationStyleUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads profile and sessions successfully`() = runTest {
        val sessions = listOf(ChatSession(id = "s1", title = "Algebra"))
        fakeChatRepo.getSessionsResult = Result.success(sessions)

        // Reload sessions to trigger
        viewModel.loadSessions()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(sessions, state.sessions)
    }

    @Test
    fun `selectSession loads messages for selected session`() = runTest {
        val session = ChatSession(id = "s1", title = "Algebra")
        val messages = listOf(ChatMessage(id = "m1", role = "user", content = "Hello"))
        fakeChatRepo.getMessagesResult = Result.success(messages)

        viewModel.selectSession(session)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("s1", state.activeSession?.id)
        assertEquals(messages, state.messages)
    }

    @Test
    fun `sendMessage success path creates session if none selected and inserts user and AI message`() = runTest {
        val newSession = ChatSession(id = "s-new", title = "New Chat")
        fakeChatRepo.getSessionsResult = Result.success(listOf(newSession))

        val aiMsg = ChatMessage(id = "m-ai", role = "ai", content = "Newton's law states that...")
        fakeChatRepo.getAiResponseResult = Result.success(
            AiChatResponse(
                reply = "Newton's law states that...",
                subject = "Physics"
            )
        )

        // Trigger input text change then send
        viewModel.onInputTextChanged("Explain Newton's law")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("new-session", state.activeSession?.id)
        assertEquals(2, state.messages.size)
        assertEquals("user", state.messages[0].role)
        assertEquals("ai", state.messages[1].role)
        assertFalse(state.isAiResponding)
    }
}
