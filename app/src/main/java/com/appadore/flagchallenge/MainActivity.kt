package com.appadore.flagchallenge

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.appadore.flagchallenge.model.AnswerFeedback
import com.appadore.flagchallenge.model.Country
import com.appadore.flagchallenge.util.Utils.getFlagDrawable
import com.appadore.flagchallenge.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlagChallengeScreen()
        }
    }

    @Composable fun FlagChallengeScreen() {
        val viewModel: GameViewModel = viewModel()
        val context = LocalContext.current

        val focusRequesterHoursFirst = FocusRequester()
        val focusRequesterHoursSecond = FocusRequester()
        val focusRequesterMinutesFirst = FocusRequester()
        val focusRequesterMinutesSecond = FocusRequester()
        val focusRequesterSecondsFirst = FocusRequester()
        val focusRequesterSecondsSecond = FocusRequester()

        LaunchedEffect(Unit) {
            viewModel.loadQuestionsFromJson(context)
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Flags Challenge", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp)) // Time Input Fields with 2 boxes for each (hours, minutes, seconds)
            if (!viewModel.challengeStarted.value) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    TimeInput(label = "Hours", firstDigit = viewModel.hoursFirstDigit, secondDigit = viewModel.hoursSecondDigit, focusRequester = focusRequesterHoursFirst, nextFocusRequester = focusRequesterHoursSecond)
                    Spacer(modifier = Modifier.width(16.dp))
                    TimeInput(label = "Minutes", firstDigit = viewModel.minutesFirstDigit, secondDigit = viewModel.minutesSecondDigit, focusRequester = focusRequesterMinutesFirst, nextFocusRequester = focusRequesterMinutesSecond)
                    Spacer(modifier = Modifier.width(16.dp))
                    TimeInput(label = "Seconds", firstDigit = viewModel.secondsFirstDigit, secondDigit = viewModel.secondsSecondDigit, focusRequester = focusRequesterSecondsFirst, nextFocusRequester = focusRequesterSecondsSecond)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = {
                    if (isValidTime(viewModel)) {
                        viewModel.scheduleChallenge()
                    } else {
                        Toast.makeText(context, "Please enter valid time!", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Save")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Timer view, hidden if the game has started
                Text(text = "Challenge Timer: ${viewModel.timerText.value}", style = MaterialTheme.typography.headlineSmall)
            }


            if (viewModel.challengeStarted.value && !viewModel.gameEnded.value) {
                Text(text = "Q${viewModel.questionNumber.value}: ${viewModel.questionText.value}", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                // Flag Image
                AsyncImage(model = getFlagDrawable(viewModel.flagUrl.value.toString()), contentDescription = "Flag", modifier = Modifier.size(100.dp) // Set size as needed
                )

                Spacer(modifier = Modifier.height(16.dp))

                OptionButtons(viewModel = viewModel, context)
            }

            if (viewModel.gameEnded.value) {
                Text(text = "Game Over! Your score: ${viewModel.score}", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = {
                    viewModel.resetGame(context) // Implement this function to reset the game state
                }) {
                    Text("Play Again")
                }
            }
        }
    }

    private fun isValidTime(viewModel: GameViewModel): Boolean {
        val hours = viewModel.hoursFirstDigit.value + viewModel.hoursSecondDigit.value
        val minutes = viewModel.minutesFirstDigit.value + viewModel.minutesSecondDigit.value
        val seconds = viewModel.secondsFirstDigit.value + viewModel.secondsSecondDigit.value

        return when {
            hours.isEmpty() && minutes.isEmpty() && seconds.isEmpty() -> false // All boxes empty
            hours.toIntOrNull() !in 0..23 -> false // Invalid hours
            minutes.toIntOrNull() !in 0..59 -> false // Invalid minutes
            seconds.toIntOrNull() !in 0..59 -> false // Invalid seconds
            else -> true // All validations passed
        }
    }

    @OptIn(ExperimentalMaterial3Api::class) @Composable fun TimeInput(label: String, firstDigit: MutableState<String>, secondDigit: MutableState<String>, focusRequester: FocusRequester? = null, nextFocusRequester: FocusRequester? = null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { // Display the label above the TextField
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) { // First digit input box
                TextField(value = firstDigit.value, onValueChange = {
                    if (it.length <= 1 && it.all { char -> char.isDigit() }) {
                        firstDigit.value = it // Move focus to the next field if input is valid
                        if (it.length == 1) {
                            nextFocusRequester?.requestFocus()
                        }
                    }
                }, modifier = Modifier
                    .width(50.dp)
                    .padding(4.dp)
                    .focusRequester(focusRequester ?: FocusRequester()), textStyle = TextStyle(textAlign = TextAlign.Center))

                // Second digit input box
                TextField(value = secondDigit.value, onValueChange = {
                    if (it.length <= 1 && it.all { char -> char.isDigit() }) {
                        secondDigit.value = it // Move focus to the next field if input is valid
                        if (it.length == 1) {
                            nextFocusRequester?.requestFocus()
                        }
                    }
                }, modifier = Modifier
                    .width(50.dp)
                    .padding(4.dp)
                    .focusRequester(nextFocusRequester ?: FocusRequester()), textStyle = TextStyle(textAlign = TextAlign.Center))
            }
        }
    }

    @Composable fun OptionButtons(viewModel: GameViewModel, context: Context) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            for (i in viewModel.options.value.indices step 2) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    OptionButton(option = viewModel.options.value[i], isSelected = viewModel.selectedOptionId.value == viewModel.options.value[i].id, isCorrect = viewModel.options.value[i].id == viewModel.correctAnswerId, feedback = viewModel.answerFeedback, onClick = {
                        viewModel.selectOption(viewModel.options.value[i].id, context)
                    })

                    Spacer(modifier = Modifier.width(16.dp))

                    // Check if there is a second button
                    if (i + 1 < viewModel.options.value.size) {
                        OptionButton(option = viewModel.options.value[i + 1], isSelected = viewModel.selectedOptionId.value == viewModel.options.value[i + 1].id, isCorrect = viewModel.options.value[i + 1].id == viewModel.correctAnswerId, feedback = viewModel.answerFeedback, onClick = {
                            viewModel.selectOption(viewModel.options.value[i + 1].id, context)
                        })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Space between rows
            }
        }
    }

    @Composable
    fun OptionButton(
        option: Country,
        isSelected: Boolean,
        isCorrect: Boolean,
        feedback: AnswerFeedback?,
        onClick: () -> Unit
    ) {
        val borderColor = when {
            feedback?.selectedOptionId == option.id && feedback.isCorrect -> Color.Green
            feedback?.selectedOptionId == option.id && !feedback.isCorrect -> Color.Red
            else -> Color.Gray
        }

        val buttonWidth = 150.dp
        val buttonHeight = 50.dp

        Button(
            onClick = onClick, // Prevent click action if disabled
            modifier = Modifier
                .size(buttonWidth, buttonHeight) // Set fixed size
                .border(BorderStroke(2.dp, borderColor))
                .padding(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
        ) {
            Text(
                text = option.countryName,
                textAlign = TextAlign.Center,
                color = Color.Black,
                maxLines = 1, // Prevent text wrapping
                overflow = TextOverflow.Ellipsis // Handle overflow with ellipsis
            )
        }
    }
}