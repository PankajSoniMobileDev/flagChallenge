package com.appadore.flagchallenge

import android.content.Context
import android.content.SharedPreferences
import com.appadore.flagchallenge.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class TimerPersistenceTest {
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var challengeViewModel: GameViewModel

    @Before
    fun setup() {
        // Mock SharedPreferences and Editor
        context = mock(Context::class.java)
        sharedPreferences = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)
        challengeViewModel = GameViewModel()
    }

    @Test
    fun `test retrieve remaining time on app reopen`() {
        // Mock the saved value
        `when`(sharedPreferences.getLong("remaining_time", 0L)).thenReturn(1800L) // 30 minutes

        // Call the method
        challengeViewModel.handleAppReopen(context)

        // Assert the remaining time is correctly set
        assertEquals(1800L, challengeViewModel.totalSecondsToStartChallenge)
    }
}