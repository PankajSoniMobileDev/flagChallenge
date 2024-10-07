package com.appadore.flagchallenge.viewmodel

import android.content.Context
import android.os.CountDownTimer
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.appadore.flagchallenge.R
import com.appadore.flagchallenge.model.AnswerFeedback
import com.appadore.flagchallenge.model.Country
import com.appadore.flagchallenge.model.Question
import com.appadore.flagchallenge.util.ChallengeWorker
import com.appadore.flagchallenge.util.GameConstants
import com.appadore.flagchallenge.util.Utils
import com.appadore.flagchallenge.util.Utils.getTargetTimeFromStorage
import com.appadore.flagchallenge.util.Utils.saveTargetTimeInStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.TimeUnit

class GameViewModel : ViewModel() {
    var hoursFirstDigit = mutableStateOf("")
    var hoursSecondDigit = mutableStateOf("")
    var minutesFirstDigit = mutableStateOf("")
    var minutesSecondDigit = mutableStateOf("")
    var secondsFirstDigit = mutableStateOf("")
    var secondsSecondDigit = mutableStateOf("")

    var timerText = mutableStateOf("00:00:00")
    var challengeStarted = mutableStateOf(false)
    var questionNumber = mutableStateOf(1)
    var questionText = mutableStateOf("Guess the Country by the Flag")
    var flagUrl = mutableStateOf(0)
    var options = mutableStateOf(listOf<Country>())
    var selectedOptionId = mutableStateOf(-1)

    var correctAnswerId: Int = 0
    var score by mutableStateOf(0)
    var answerFeedback by mutableStateOf<AnswerFeedback?>(null)
    private var gameOver by mutableStateOf(false)
    private var finalScore by mutableStateOf(0)

    var totalSecondsToStartChallenge: Int = 0
    private var countdownJob: Job? = null
    private var questionList = listOf<Question>()
    var gameEnded = mutableStateOf(false)
    var timeLeftInMillis = mutableStateOf(GameConstants.QUESTION_TIME_MILLIS)
    var isAnswerSelected = mutableStateOf(false)

    private var countDownTimer: CountDownTimer? = null


    /**
     * method used to start the timer for every question
     */
    fun startTimer() {
        countDownTimer?.cancel()
        timeLeftInMillis.value = GameConstants.QUESTION_TIME_MILLIS
        isAnswerSelected.value = false

        countDownTimer = object : CountDownTimer(GameConstants.QUESTION_TIME_MILLIS, GameConstants.TIMER_INTERVAL_MILLIS) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis.value = millisUntilFinished
            }

            override fun onFinish() {
                if (!isAnswerSelected.value) {
                    questionNumber.value++
                    loadNextQuestion()
                }
            }
        }.start()
    }

    /**
     * method used to load the question data form json
     */
    fun loadQuestionsFromJson(context: Context) {

        val inputStream: InputStream = context.resources.openRawResource(R.raw.questions)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val questionsJson = jsonObject.getJSONArray("questions")
        val questions = mutableListOf<Question>()

        for (i in 0 until questionsJson.length()) {
            val questionJson = questionsJson.getJSONObject(i)
            val answerId = questionJson.getInt("answer_id")
            val countryCode = questionJson.getString("country_code")
            val optionsJson = questionJson.getJSONArray("countries")
            val options = mutableListOf<Country>()

            for (j in 0 until optionsJson.length()) {
                val optionJson = optionsJson.getJSONObject(j)
                val countryName = optionJson.getString("country_name")
                val id = optionJson.getInt("id")
                options.add(Country(countryName, id))
            }

            questions.add(Question(answerId, options, countryCode))
        }

        questionList = questions
        loadNextQuestion()
    }

    /**
     * method used to schedule the challenge
     */
    fun scheduleChallenge(context: Context) {
        val hours = getTimeFromDigits(hoursFirstDigit.value, hoursSecondDigit.value)
        val minutes = getTimeFromDigits(minutesFirstDigit.value, minutesSecondDigit.value)
        val seconds = getTimeFromDigits(secondsFirstDigit.value, secondsSecondDigit.value)

        if (isValidTime(hours, minutes, seconds)) {
            totalSecondsToStartChallenge = hours * 3600 + minutes * 60 + seconds

            val startTime = System.currentTimeMillis()
            val sharedPreferences = context.getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putLong("countdown_start_time", startTime)
                .putInt("total_seconds_scheduled", totalSecondsToStartChallenge)
                .apply()

            scheduleWorkManagerTimer(context,startTime, totalSecondsToStartChallenge)

            startCountdown(context)
        } else {
            timerText.value = "Invalid Time!"
        }
    }

    /**
     * method used to start the countdown timer
     */
    fun startCountdown(context: Context) {
        countdownJob?.cancel()
        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            while (totalSecondsToStartChallenge > 0) {
                delay(GameConstants.TIMER_INTERVAL_MILLIS)
                totalSecondsToStartChallenge--
                updateTimerText(totalSecondsToStartChallenge)
                if (totalSecondsToStartChallenge == 20) {
                    timerText.value = "CHALLENGE WILL START IN 00:20"
                }
            }
            if (totalSecondsToStartChallenge == 0) {
                startChallenge(context)
            }
        }
    }

    /**
     * method used to update the timer text
     */
    private fun updateTimerText(totalSeconds: Int) {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        timerText.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * method used to load the next question
     */
    private fun loadNextQuestion() {
        isAnswerSelected.value = false
        answerFeedback = null

        if (questionNumber.value <= questionList.size) {
            val currentQuestion = questionList[questionNumber.value - 1]
            flagUrl.value = Utils.getFlagDrawable(currentQuestion.countryCode)
            options.value = currentQuestion.countries
            questionText.value = "Guess the Country by the Flag"
        } else {
            gameOver = true  // Set game over state
            finalScore = score // Store the final score
            questionText.value = "Challenge Completed! Your score: $finalScore"
        }
    }

    /**
     * method used to handle the selected options
     */
    fun selectOption(optionId: Int, context: Context) {
        if (gameOver || isAnswerSelected.value) return

        val currentQuestion = questionList[questionNumber.value - 1]

        val isCorrect = optionId == currentQuestion.answerId

        answerFeedback = AnswerFeedback(isCorrect, optionId,correctOptionId = currentQuestion.answerId)

        if (isCorrect) {
            selectedOptionId.value = optionId
            score += GameConstants.CORRECT_ANSWER_SCORE
            Toast.makeText(context, "Correct Answer!", Toast.LENGTH_SHORT).show()
        } else {
            selectedOptionId.value = optionId
            Toast.makeText(context, "Incorrect Answer. Try Again!", Toast.LENGTH_SHORT).show()
        }

        isAnswerSelected.value = true
        countDownTimer?.cancel()

        viewModelScope.launch {
            if (questionNumber.value >= questionList.size) {
                endGame()
            } else {
                delay(GameConstants.NEXT_QUESTION_DELAY)
                questionNumber.value++
                loadNextQuestion()
            }
        }
    }

    /**
     * method used to get the digits from time
     */
    private fun getTimeFromDigits(firstDigit: String, secondDigit: String): Int {
        return (firstDigit.toIntOrNull() ?: 0) * 10 + (secondDigit.toIntOrNull() ?: 0)
    }

    /**
     * method used to check if user entered the correct values in time
     */
    private fun isValidTime(hours: Int, minutes: Int, seconds: Int): Boolean {
        return hours in 0..23 && minutes in 0..59 && seconds in 0..59
    }

    /**
     * method used to start the challenge once timer ended
     */
    private fun startChallenge(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("challenge_timer")
        challengeStarted.value = true
        loadNextQuestion()
    }

    /**
     * method used to reset the game once ended
     */
    fun resetGame(context: Context) {
        score = 0
        questionNumber.value = 1
        selectedOptionId.value = -1
        gameOver = false
        finalScore = 0
        gameEnded.value = false
        timerText.value = "00:00:00"
        challengeStarted.value = false
        hoursFirstDigit.value=""
        hoursSecondDigit.value=""
        minutesFirstDigit.value=""
        minutesSecondDigit.value=""
        secondsFirstDigit.value=""
        secondsSecondDigit.value=""
        loadQuestionsFromJson(context = context)
        WorkManager.getInstance(context).cancelAllWorkByTag("challenge_timer")
    }

    /**
     * method used to update the value of game ended
     */
    private fun endGame() {
        gameEnded.value = true
    }

    /**
     * method used to handle timer scenario
     */
    fun handleAppReopen(context: Context) {
        val currentTime = System.currentTimeMillis()
        val sharedPreferences = context.getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)
        val savedStartTime = sharedPreferences.getLong("countdown_start_time", 0L)
        val totalSecondsScheduled = sharedPreferences.getInt("total_seconds_scheduled", 0)

        if (savedStartTime == 0L || totalSecondsScheduled == 0) {
            timerText.value = "Set the time to start the challenge"
            return
        }

        val elapsedTimeInSeconds = ((currentTime - savedStartTime) / 1000).toInt()
        val remainingTime = totalSecondsScheduled - elapsedTimeInSeconds

        if (remainingTime > 0) {
            totalSecondsToStartChallenge = remainingTime
            startCountdown(context)
        } else {
            startChallenge(context)
        }

        WorkManager.getInstance(context).cancelAllWork()
    }

    /**
     * starting work manager for the timer
     */
    private fun scheduleWorkManagerTimer(context: Context,startTime: Long, totalSecondsScheduled: Int) {
        val inputData = workDataOf(
            "startTime" to startTime,
            "totalSecondsScheduled" to totalSecondsScheduled
        )

        val delayInMillis = totalSecondsScheduled * 1000L
        val workRequest = OneTimeWorkRequestBuilder<ChallengeWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("challenge_timer")
            .build()

        WorkManager.getInstance(context)
            .enqueue(workRequest)
    }
}