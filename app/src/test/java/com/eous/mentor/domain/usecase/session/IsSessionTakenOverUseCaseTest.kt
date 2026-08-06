package com.eous.mentor.domain.usecase.session

import com.eous.mentor.testutil.FakeSessionRepository
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsSessionTakenOverUseCaseTest {

    private val fakeRepo = FakeSessionRepository()
    private val useCase = IsSessionTakenOverUseCase(fakeRepo)
    private val context = mockk<android.content.Context>() // Context is not used by FakeSessionRepository

    @Test
    fun `returns false when remoteSessionId is null`() {
        val result = useCase(context, null, treatMissingLocalAsTakenOver = true)
        assertFalse(result)
    }

    @Test
    fun `returns false when remoteSessionId is empty`() {
        val result = useCase(context, "", treatMissingLocalAsTakenOver = true)
        assertFalse(result)
    }

    @Test
    fun `returns false when local matches remote`() {
        fakeRepo.localSessionId = "session-123"
        val result = useCase(context, "session-123", treatMissingLocalAsTakenOver = true)
        assertFalse(result)
    }

    @Test
    fun `returns true when local does not match remote`() {
        fakeRepo.localSessionId = "session-123"
        val result = useCase(context, "session-456", treatMissingLocalAsTakenOver = true)
        assertTrue(result)
    }

    @Test
    fun `returns true when local is empty and treatMissingLocalAsTakenOver is true`() {
        fakeRepo.localSessionId = ""
        val result = useCase(context, "session-123", treatMissingLocalAsTakenOver = true)
        assertTrue(result)
    }

    @Test
    fun `returns false when local is empty and treatMissingLocalAsTakenOver is false`() {
        fakeRepo.localSessionId = ""
        val result = useCase(context, "session-123", treatMissingLocalAsTakenOver = false)
        assertFalse(result)
    }
}
