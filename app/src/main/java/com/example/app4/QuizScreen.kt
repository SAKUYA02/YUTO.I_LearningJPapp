package com.example.app4

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


data class QuizQuestion(val id: String, val questionText: String, val options: List<String>, val correctAnswerIndex: Int, val difficulty: String)

@Composable
fun QuizScreen(
    navController: NavHostController,
    context: Context,
    adaptiveLearningManager: AdaptiveLearningManager,
    initialDifficulty: String = "easy",
    initialQuestionId: String? = null
) {
    var allQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var difficulty by remember { mutableStateOf(initialDifficulty) }
    var questions by remember { mutableStateOf(emptyList<QuizQuestion>()) }
    var currentIndex by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf(-1) }
    var score by remember { mutableStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var highScore by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var isNewHighScore by remember { mutableStateOf(false) }
    var selectedQuestionCount by remember { mutableStateOf(0) }
    var showQuestionCountSelection by remember { mutableStateOf(true) }
    var totalQuestions by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        startTime = System.currentTimeMillis()
        onDispose {
            val duration = System.currentTimeMillis() - startTime
            adaptiveLearningManager.recordLearningActivity("quiz", duration)
        }
    }

    fun initializeQuiz() {
        val filteredQuestions = adaptiveLearningManager.getQuizQuestionsByDifficulty(allQuestions, difficulty)
        totalQuestions = filteredQuestions.size
        questions = when (selectedQuestionCount) {
            10 -> filteredQuestions.shuffled().take(10)
            30 -> filteredQuestions.shuffled().take(30)
            else -> filteredQuestions.shuffled()
        }
        if (initialQuestionId != null) {
            currentIndex = questions.indexOfFirst { it.id == initialQuestionId }.coerceAtLeast(0)
        } else {
            currentIndex = 0
        }
        score = 0
        isCompleted = false
        selectedAnswerIndex = -1
        showAnswer = false
        highScore = adaptiveLearningManager.getHighScore(difficulty)
        showQuestionCountSelection = false
    }

    LaunchedEffect(Unit) {
        adaptiveLearningManager.incrementQuizAttemptCount(difficulty)
        try {
            allQuestions = withContext(Dispatchers.IO) {
                loadQuizJsonData(context, "quiz_questions.json")
            }
            isLoading = false
        } catch (e: Exception) {
            error = "Failed to load question: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Quiz (${if (difficulty == "easy") "Word" else "Grammar"})",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        difficulty = if (difficulty == "easy") "hard" else "easy"
                        showQuestionCountSelection = true
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (difficulty == "easy") "Switch to grammar quiz" else "Switch to word quiz")
                }

                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    error != null -> Text(text = error!!, color = MaterialTheme.colorScheme.error)
                    showQuestionCountSelection -> {
                        Text("How many questions would you like to answer?")
                        Button(onClick = { selectedQuestionCount = 10; initializeQuiz() }) {
                            Text("10 questions")
                        }
                        Button(onClick = { selectedQuestionCount = 30; initializeQuiz() }) {
                            Text("30 questions")
                        }
                        Button(onClick = { selectedQuestionCount = 0; initializeQuiz() }) {
                            Text("All questions (${if (difficulty == "easy") "98" else "96"})")
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("home") },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("Back to Main Menu")
                        }
                    }
                    questions.isEmpty() -> Text("No question with this difficulty level", modifier = Modifier.align(Alignment.CenterHorizontally))
                    !isCompleted && currentIndex < questions.size -> {
                        val question = questions[currentIndex]
                        Text(text = "Question ${currentIndex + 1} / ${questions.size}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = question.questionText)
                        Spacer(modifier = Modifier.height(8.dp))
                        question.options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedAnswerIndex == index,
                                    onClick = {
                                        if (!showAnswer) {
                                            selectedAnswerIndex = index
                                        }
                                    },
                                    enabled = !showAnswer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = option)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (showAnswer) {
                            if (selectedAnswerIndex == question.correctAnswerIndex) {
                                Text(text = "Correct!", color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text(text = "Wrong, correct answer is: ${question.options[question.correctAnswerIndex]}", color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    showAnswer = false
                                    currentIndex++
                                    selectedAnswerIndex = -1
                                    if (currentIndex >= questions.size) {
                                        isCompleted = true
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Next")
                            }
                        } else {
                            Row {
                                Button(
                                    onClick = {
                                        showAnswer = true
                                        val isCorrect = selectedAnswerIndex == question.correctAnswerIndex
                                        if (isCorrect) {
                                            score++
                                        } else {
                                            adaptiveLearningManager.recordMistake(question.id)
                                        }
                                        adaptiveLearningManager.updateCorrectRate(
                                            if (difficulty == "easy") "word" else "grammar",
                                            if (isCorrect) 1f else 0f
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = selectedAnswerIndex != -1
                                ) {
                                    Text("Check")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        navController.navigate("home")
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Quit")
                                }
                            }
                        }
                    }
                    else -> {
                        if (isCompleted) {
                            val correctRate = score.toFloat() / questions.size
                            adaptiveLearningManager.updateCorrectRate(if (difficulty == "easy") "word" else "grammar", correctRate)
                            adaptiveLearningManager.updateHighScore(difficulty, score)
                            adaptiveLearningManager.checkAndAwardBadges()
                        }
                        Text(text = "Quiz completed! Score: $score / ${questions.size}")

                        // ハイスコアの更新処理
                        if (score > highScore) {
                            highScore = score
                            isNewHighScore = true
                            adaptiveLearningManager.updateHighScore(difficulty, score)
                        }

                        if (isNewHighScore) {
                            Text(text = "New record！🎉", style = MaterialTheme.typography.headlineSmall)
                        }
                        Text(text = "All-time high score: $highScore")
                        Text(text = "Percentage correct this time: ${(score.toDouble() / questions.size * 100).toInt()}%")
                        Text(text = "Overall percentage correct: ${(adaptiveLearningManager.getCorrectRate(if (difficulty == "easy") "word" else "grammar") * 100).toInt()}%")
                        if (selectedQuestionCount == 0 && score == totalQuestions) {
                            adaptiveLearningManager.updateHighScore(difficulty, score)
                            adaptiveLearningManager.saveBadgeStatus("perfect_quiz_${difficulty}", true)
                            Text(text = "Congratulations! You have a perfect score on all questions! 🏅", style = MaterialTheme.typography.headlineSmall)
                        } else {
                            adaptiveLearningManager.updateHighScore(difficulty, score)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                initializeQuiz()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                navController.navigate("home")
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Back to Main Menu")
                        }
                    }
                }
            }
        }
    )
}

suspend fun loadQuizQuestions(context: Context): List<QuizQuestion> {
    return withContext(Dispatchers.IO) {
        loadQuizJsonData(context, "quiz_questions.json")
    }
}

suspend fun loadQuizJsonData(context: Context, fileName: String): List<QuizQuestion> {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        throw IOException("Failed to read JSON file: ${ioException.message}")
    }

    val jsonArray = JSONArray(jsonString)
    val questions = mutableListOf<QuizQuestion>()
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val questionText = jsonObject.getString("questionText")
        val options = jsonObject.getJSONArray("options")
        val correctAnswerIndex = jsonObject.getInt("correctAnswerIndex")
        val difficulty = jsonObject.getString("difficulty")
        val optionsList = mutableListOf<String>()
        for (j in 0 until options.length()) {
            optionsList.add(options.getString(j))
        }
        // 動的に ID を生成
        val id = "${difficulty}_${i + 1}"
        questions.add(QuizQuestion(id, questionText, optionsList, correctAnswerIndex, difficulty))
    }

    if (questions.isEmpty()) {
        throw IllegalStateException("No valid issue found")
    }

    return questions
}