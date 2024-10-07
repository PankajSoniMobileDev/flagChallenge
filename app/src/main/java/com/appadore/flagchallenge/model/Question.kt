package com.appadore.flagchallenge.model

data class Question(
    val answerId: Int,
    val countries: List<Country>,
    val countryCode: String
)

data class Country(
    val countryName: String,
    val id: Int
)

data class AnswerFeedback(
    val isCorrect: Boolean,   // Indicates if the selected answer is correct
    val selectedOptionId: Int,
    val correctOptionId: Int// The ID of the selected option
)
