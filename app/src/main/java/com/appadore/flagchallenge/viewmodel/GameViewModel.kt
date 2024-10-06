package com.appadore.flagchallenge.viewmodel

import android.content.Context
import android.os.CountDownTimer
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appadore.flagchallenge.R
import com.appadore.flagchallenge.model.AnswerFeedback
import com.appadore.flagchallenge.model.Country
import com.appadore.flagchallenge.model.Question
import com.appadore.flagchallenge.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream

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
    var gameOver by mutableStateOf(false)  // New variable to track if the game is over
    var finalScore by mutableStateOf(0)   // New variable to store the final score

    private var totalSecondsToStartChallenge: Int = 0
    private var countdownJob: Job? = null
    private var questionList = listOf<Question>()
    var gameEnded = mutableStateOf(false)

    // Load JSON and parse the data into questionList
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

    fun scheduleChallenge() {
        val hours = getTimeFromDigits(hoursFirstDigit.value, hoursSecondDigit.value)
        val minutes = getTimeFromDigits(minutesFirstDigit.value, minutesSecondDigit.value)
        val seconds = getTimeFromDigits(secondsFirstDigit.value, secondsSecondDigit.value)

        if (isValidTime(hours, minutes, seconds)) {
            totalSecondsToStartChallenge = hours * 3600 + minutes * 60 + seconds
            startCountdown()
        } else {
            timerText.value = "Invalid Time!"
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            while (totalSecondsToStartChallenge > 0) {
                delay(1000)
                totalSecondsToStartChallenge--
                updateTimerText(totalSecondsToStartChallenge)
                if (totalSecondsToStartChallenge == 20) {
                    timerText.value = "CHALLENGE WILL START IN 00:20"
                }
            }
            if (totalSecondsToStartChallenge == 0) {
                startChallenge()
            }
        }
    }

    private fun updateTimerText(totalSeconds: Int) {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        timerText.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun loadNextQuestion() {
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

    fun selectOption(optionId: Int, context: Context) {
        if (gameOver) return // Do nothing if the game is over

        val currentQuestion = questionList[questionNumber.value - 1]

        // Check if the selected option is correct
        val isCorrect = optionId == currentQuestion.answerId

        // Provide feedback based on the answer's correctness
        answerFeedback = AnswerFeedback(isCorrect, optionId)

        if (isCorrect) { // Correct answer logic
            selectedOptionId.value = optionId
            score += 10 // Increment score for correct answer
            Toast.makeText(context, "Correct Answer!", Toast.LENGTH_SHORT).show()
        } else { // Wrong answer logic
            selectedOptionId.value = optionId
            Toast.makeText(context, "Incorrect Answer. Try Again!", Toast.LENGTH_SHORT).show()
        }

        // Move to the next question after a delay if the answer was selected
        viewModelScope.launch {
            if (questionNumber.value >= questionList.size) {
                endGame() // End the game if this is the last question
            } else {
                delay(2000) // Hold for 2 seconds before moving to the next question
                questionNumber.value++
                loadNextQuestion()
            }
        }
    }

    private fun getTimeFromDigits(firstDigit: String, secondDigit: String): Int {
        return (firstDigit.toIntOrNull() ?: 0) * 10 + (secondDigit.toIntOrNull() ?: 0)
    }

    private fun isValidTime(hours: Int, minutes: Int, seconds: Int): Boolean {
        return hours in 0..23 && minutes in 0..59 && seconds in 0..59
    }

    private fun startChallenge() {
        challengeStarted.value = true
        loadNextQuestion()
    }

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
    }

    fun endGame() {
        gameEnded.value = true
    }
}