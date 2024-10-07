Flags Challenge Game

Overview

Flags Challenge is an interactive quiz game designed to test players’ knowledge of world flags. Players will answer multiple-choice questions, selecting the correct country for a given flag. The game incorporates a timer, score tracking, and a friendly user interface built with Jetpack Compose.

Table of Contents

	•	Features
	•	Technologies Used
	•	Getting Started
	•	Gameplay Instructions
	•	Game Logic and Timing
	•	WorkManager Implementation
	•	Code Structure
	•	Future Improvements
	•	Contributing

Features

	•	Multiple-Choice Questions: Test your knowledge with various questions about country flags.
	•	Scoring System: Earn points for each correct answer.
	•	Countdown Timer: Challenge yourself with a time limit for each question.
	•	Feedback Mechanism: Get immediate feedback on your answer choices.
	•	Replay Functionality: Option to restart the game after completing a round.

Technologies Used

	•	Programming Language: Kotlin
	•	UI Framework: Jetpack Compose for building modern UIs.
	•	Dependency Injection: Hilt for dependency management.
	•	Data Handling: JSON files for storing questions and answers.
	•	Development Environment: Android Studio for building and testing the app.

Getting Started

Prerequisites

Make sure you have the following installed on your development machine:

	•	Android Studio
	•	Kotlin SDK
	•	Emulator or physical Android device for testing

Installation

	1.	Clone the repository:
                git clone https://github.com/PankajSoniMobileDev/flagChallenge.git

  2.	Open the project in Android Studio:
	•	Launch Android Studio.
	•	Click on “Open an existing Android Studio project.”
	•	Navigate to the cloned repository folder and open it.
	3.	Sync the project with Gradle:
	•	Click on “Sync Project with Gradle Files” in the toolbar.
	4.	Run the app:
	•	Connect your Android device or start an emulator.
	•	Click the “Run” button in Android Studio.

Gameplay Instructions

	1.	Start the Game:
	•	Launch the app and view the timer.
	•	Click on the “Save” button to begin the quiz.
	2.	Answering Questions:
	•	You will see a question with a flag and multiple-choice options.
	•	Select an option within the 2-second time limit.
	•	Immediate feedback will be provided after selection.
	3.	Game Progression:
	•	Correct answers will increase your score.
	•	If you answer incorrectly, you can try again for the same question until the time runs out.
	4.	Game End:
	•	Once all questions have been answered, the game will display your final score.
	•	You can choose to play again by clicking the “Play Again” button.

Game Logic and Timing

	•	Questions: Each question consists of a flag image and multiple answer options, which players must select within a limited time frame.
	•	Answering Timing: Players have 2 seconds to choose an answer. If they do not select an option within this time, the game automatically moves to the next question.
	•	Score Calculation: Correct answers increase the score by 10 points. The timer resets after each question, ensuring players face a new countdown for every question.

WorkManager Implementation

	•	Background Timer: The app utilizes WorkManager to manage the countdown timer in the background, ensuring that even if the app is closed, the timer continues to function.
	•	Notification Alerts: Notifications are triggered by the WorkManager to inform users about remaining time and when the challenge starts.
	•	Persistence: The timer maintains its state across app restarts, allowing users to pick up right where they left off.

Code Structure

The project is structured in a modular way for better maintainability:

	•	MainActivity.kt: Entry point of the app, setting up the game environment.
	•	ViewModel: Contains game logic and state management.
	•	UI Components: Composable functions that define the user interface, such as:
	•	OptionButton: Displays the answer options as buttons.
	•	TimerView: Shows the countdown timer.
	•	GameOverView: Displays the final score and options to play again.

Example of Option Button Code
@Composable
fun OptionButton(option: Country, isSelected: Boolean, isCorrect: Boolean, feedback: AnswerFeedback?, onClick: () -> Unit) {
    // Set button styles based on user interactions
}

Future Improvements

	•	Enhanced User Interface: Implement animations and transitions for a smoother experience.
	•	Difficulty Levels: Add different difficulty levels with varying numbers of questions and time limits.
	•	Leaderboard: Introduce a scoring system that allows players to compete against others.
	•	Localization: Support multiple languages for international audiences.

Contributing

Contributions are welcome! If you would like to contribute, please follow these steps:

	1.	Fork the repository:
	•	Click on the “Fork” button at the top right of the repository page.
	2.	Create a new branch:
 git checkout -b feature/YourFeature

 	3.	Make your changes:
	•	Implement your feature or fix in the code.
	4.	Commit your changes:
 git commit -m 'Add some feature'
	5.	Push to the branch:
 git push origin feature/YourFeature

 	6.	Create a pull request:
	•	Go to the original repository and click “New Pull Request.”

 
