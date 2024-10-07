package com.appadore.flagchallenge

import android.content.Context
import com.appadore.flagchallenge.viewmodel.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class TimerTest {
    private lateinit var context: Context
    private lateinit var challengeViewModel: GameViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = Mockito.mock(Context::class.java)
        challengeViewModel = GameViewModel()
    }

    @After
    fun tearDown() {
        // Reset the Main dispatcher to its original state
        Dispatchers.resetMain()
    }

    @Test
    fun `test timer countdown works correctly`() = runTest {
        challengeViewModel.startCountdown(context)

        // Initially, assert the timer should show "00:01:00" or equivalent representation for 60 seconds
        assertEquals("00:01:00", challengeViewModel.timerText.value)

        // Advance virtual time by 1 second (1000 milliseconds)
        advanceTimeBy(1000L)  // 1 second in milliseconds

        // Assert that the timer updates to 59 seconds
        assertEquals("00:00:59", challengeViewModel.timerText.value)

        // Advance time by another 59 seconds to complete the countdown
        advanceTimeBy(59000L)  // Advance to complete the 60 seconds

        // The timer should now be at 0
        assertEquals("00:00:00", challengeViewModel.timerText.value)

        // Optionally, assert that the challenge has started or any other logic you've implemented
        // For example, if there is a boolean indicating challenge start:
        assertTrue(challengeViewModel.challengeStarted.value)
    }
}