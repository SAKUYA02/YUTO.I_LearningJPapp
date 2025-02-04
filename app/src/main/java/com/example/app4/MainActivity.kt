package com.example.app4

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.foundation.clickable
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.ArrowBack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app4.ui.theme.App4Theme

class MainActivity : ComponentActivity() {
    private lateinit var adaptiveLearningManager: AdaptiveLearningManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adaptiveLearningManager = AdaptiveLearningManager(applicationContext)
        enableEdgeToEdge()
        setContent {
            App4Theme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController, applicationContext) }
                    composable("signup") { SignUpScreen(navController, applicationContext) }
                    composable("home") { HomeScreen(navController, applicationContext, adaptiveLearningManager) }
                    composable("word_study") { WordStudyScreen(navController, applicationContext, adaptiveLearningManager) }
                    composable("grammar_study") { GrammarStudyScreen(navController, applicationContext, adaptiveLearningManager) }
                    composable("quiz") { QuizScreen(navController, applicationContext, adaptiveLearningManager) }
                    composable("favorites") { FavoritesScreen(navController, applicationContext, adaptiveLearningManager) }
                    composable("analysis") { AnalysisScreen(navController, adaptiveLearningManager) }
                    composable("review") { ReviewScreen(navController, adaptiveLearningManager) }
                    composable("badges") { BadgesScreen(navController, adaptiveLearningManager) }
                    composable("tiktok_player") { TikTokPlayerScreen(navController) }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController, context: Context) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginFailed by remember { mutableStateOf(false) }

    fun loginUser(name: String, password: String): Boolean {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedPassword = sharedPreferences.getString(name, null)
        if (savedPassword == password) {
            sharedPreferences.edit().putString("current_user", name).apply()
            return true
        }
        return false
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
                    text = "NoNoNo Japanese",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (loginFailed) {
                    Text(
                        text = "Login failed. Please check your name and password.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (loginUser(name, password)) {
                            navController.navigate("home")
                        } else {
                            loginFailed = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { navController.navigate("signup") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't have an account? Sign Up")
                }
            }
        }
    )
}

@Composable
fun SignUpScreen(navController: NavHostController, context: Context) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var signUpFailed by remember { mutableStateOf(false) }

    fun signUpUser(name: String, password: String) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(name, password)
            apply()
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
                    text = "NoNoNo Japanese",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (signUpFailed) {
                    Text(
                        text = "Sign up failed. Passwords do not match.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (password == confirmPassword) {
                            signUpUser(name, password)
                            navController.navigate("login")
                        } else {
                            signUpFailed = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Up")
                }
            }
        }
    )
}

@Composable
fun HomeScreen(navController: NavHostController, context: Context, adaptiveLearningManager: AdaptiveLearningManager) {
    var recommendations by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        recommendations = adaptiveLearningManager.getRecommendations()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                Text(
                    text = "NoNoNo Japanese - Home",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (recommendations.isNotEmpty()) {
                    Text(
                        text = "Learning Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    recommendations.forEach { recommendation ->
                        Text(
                            text = "• $recommendation",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(
                        text = "There are currently no recommendations",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = { navController.navigate("word_study") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Word study")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("grammar_study") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grammar study")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("quiz") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quiz")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("favorites") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Favorite")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("analysis") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Learning Analysis")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("review") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Review")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("badges") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Badge List")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("tiktok_player") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("TikTok Player")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        logout(context)
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    )
}

fun logout(context: Context) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove("current_user").apply()
}

@Composable
fun FavoritesScreen(navController: NavHostController, context: Context, adaptiveLearningManager: AdaptiveLearningManager) {
    val favoriteWords = remember { adaptiveLearningManager.getFavoriteWords() }
    val favoriteGrammars = remember { adaptiveLearningManager.getFavoriteGrammars() }
    var showWords by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Favorite",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showWords = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showWords) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Word")
                    }
                    Button(
                        onClick = { showWords = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showWords) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Grammar")
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (showWords) {
                        item {
                            Text(
                                text = "Favorite words",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(favoriteWords) { word ->
                            Text(
                                text = word,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        val index = adaptiveLearningManager.getWordIndex(word)
                                        adaptiveLearningManager.saveStudyProgress("word", index)
                                        navController.navigate("word_study")
                                    }
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "Favorite grammar",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(favoriteGrammars) { grammar ->
                            Text(
                                text = grammar,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        val index = adaptiveLearningManager.getGrammarIndex(grammar)
                                        adaptiveLearningManager.saveStudyProgress("grammar", index)
                                        navController.navigate("grammar_study")
                                    }
                            )
                        }
                    }
                }

                Button(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                ) {
                    Text("Back to Main Menu")
                }
            }
        }
    )
}


@Composable
fun ReviewScreen(navController: NavHostController, adaptiveLearningManager: AdaptiveLearningManager) {
    val context = LocalContext.current
    var words by remember { mutableStateOf(emptyList<Word>()) }
    var grammars by remember { mutableStateOf(emptyList<Grammar>()) }
    var allReviewItems by remember { mutableStateOf(emptyList<String>()) }
    var currentReviewItem by remember { mutableStateOf<String?>(null) }
    var showAnswer by remember { mutableStateOf(false) }
    var reviewCompleted by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }
    var allQuestions by remember { mutableStateOf(emptyList<QuizQuestion>()) }

    LaunchedEffect(Unit) {
        words = adaptiveLearningManager.loadWords(context)
        grammars = adaptiveLearningManager.loadGrammars(context)
        allQuestions = loadQuizQuestions(context)

        // ユーザーごとの復習アイテムを取得
        val wordReviewItems = adaptiveLearningManager.getItemsDueForReview("word")
        val grammarReviewItems = adaptiveLearningManager.getItemsDueForReview("grammar")
        val mistakeReviewItems = adaptiveLearningManager.getReviewList()

        allReviewItems = (wordReviewItems.map { "word:$it" } +
                grammarReviewItems.map { "grammar:$it" } +
                mistakeReviewItems.map { "mistake:$it" }).shuffled()

        if (allReviewItems.isNotEmpty()) {
            currentReviewItem = allReviewItems.first()
        } else {
            reviewCompleted = true
        }
    }

    fun nextReviewItem() {
        showAnswer = false
        val (type, content) = currentReviewItem!!.split(":", limit = 2)
        when (type) {
            "word", "grammar" -> adaptiveLearningManager.scheduleReview(content, type, true)
            "mistake" -> adaptiveLearningManager.removeFromReviewList(content)
        }
        val nextIndex = currentIndex + 1
        if (nextIndex < allReviewItems.size) {
            currentReviewItem = allReviewItems[nextIndex]
            currentIndex = nextIndex
        } else {
            reviewCompleted = true
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
                    text = "Review",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (reviewCompleted) {
                    Text("Congratulations! You have reviewed all")
                    Button(
                        onClick = { navController.navigate("home") },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Back to Main Menu")
                    }
                } else if (currentReviewItem != null) {
                    Text(
                        text = "Question ${currentIndex + 1} / ${allReviewItems.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            val (type, content) = currentReviewItem!!.split(":", limit = 2)
                            when (type) {
                                "word" -> {
                                    val word = words.find { it.word == content }!!
                                    Text(text = word.word, style = MaterialTheme.typography.headlineSmall)
                                    Text("How to read: ${word.reading}")
                                    if (showAnswer) {
                                        Text("Meaning: ${word.meaning}")
                                    }
                                }
                                "grammar" -> {
                                    val grammar = grammars.find { it.grammar == content }!!
                                    Text(text = grammar.grammar, style = MaterialTheme.typography.headlineSmall)
                                    if (showAnswer) {
                                        Text("Example: ${grammar.example}")
                                    }
                                }
                                "mistake" -> {
                                    val question = allQuestions.find { it.id == content }!!
                                    Text(text = question.questionText, style = MaterialTheme.typography.headlineSmall)
                                    if (showAnswer) {
                                        Text("Correct: ${question.options[question.correctAnswerIndex]}")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { showAnswer = !showAnswer }) {
                            Text(if (showAnswer) "Hide" else "Show answer")
                        }
                        Button(onClick = { nextReviewItem() }) {
                            Text("Next")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("home") },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Quit")
                    }
                }
            }
        }
    )
}

@Composable
fun AnalysisScreen(navController: NavHostController, adaptiveLearningManager: AdaptiveLearningManager) {
    val wordLearningTime = adaptiveLearningManager.getLearningTime("word")
    val grammarLearningTime = adaptiveLearningManager.getLearningTime("grammar")
    val learningStreak = adaptiveLearningManager.getLearningStreak()
    val easyQuizAttemptCount = adaptiveLearningManager.getQuizAttemptCount("easy")
    val hardQuizAttemptCount = adaptiveLearningManager.getQuizAttemptCount("hard")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Learning Analysis",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("Quiz", style = MaterialTheme.typography.titleMedium)
                Text("Number of attempts in word mode: $easyQuizAttemptCount times")
                Text("Number of attempts in grammar mode: $hardQuizAttemptCount times")

                Text("Word study", style = MaterialTheme.typography.titleMedium)
                val wordCorrectRate = adaptiveLearningManager.getCorrectRate("word")
                Text("Percentage of correct answers: ${(wordCorrectRate * 100).toInt()}%")
                val wordHighScore = adaptiveLearningManager.getHighScore("easy")
                Text("High Score: $wordHighScore")
                Spacer(modifier = Modifier.height(16.dp))

                Text("Grammar study", style = MaterialTheme.typography.titleMedium)
                val grammarCorrectRate = adaptiveLearningManager.getCorrectRate("grammar")
                Text("Percentage of correct answers: ${(grammarCorrectRate * 100).toInt()}%")
                val grammarHighScore = adaptiveLearningManager.getHighScore("hard")
                Text("High Score: $grammarHighScore")
                Spacer(modifier = Modifier.height(16.dp))

                Text("Overall learning progress", style = MaterialTheme.typography.titleMedium)
                val overallProgress = (wordCorrectRate + grammarCorrectRate) / 2
                Text("Overall Correct Percentage: ${(overallProgress * 100).toInt()}%")

                val wordProgress = adaptiveLearningManager.getLearningProgress("word")
                val grammarProgress = adaptiveLearningManager.getLearningProgress("grammar")
                Text("Word Study Progress: ${(wordProgress * 100).toInt()}%")
                Text("Grammar Study Progress: ${(grammarProgress * 100).toInt()}%")

                Spacer(modifier = Modifier.height(16.dp))

                Text("Study Time", style = MaterialTheme.typography.titleMedium)
                Text("Word Study Time: ${wordLearningTime / (60 * 1000)} miniutes")
                Text("Grammar Study Time: ${grammarLearningTime / (60 * 1000)} minutes")

                Spacer(modifier = Modifier.height(16.dp))

                Text("Learning Streaks", style = MaterialTheme.typography.titleMedium)
                Text("Number of consecutive study days: $learningStreak ")

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Back to Main Menu")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(navController: NavHostController, adaptiveLearningManager: AdaptiveLearningManager) {
    adaptiveLearningManager.checkAndAwardBadges()
    val badges = adaptiveLearningManager.getBadges()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = { Text("Badge List") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(badges) { badge ->
                BadgeItem(badge, adaptiveLearningManager)
            }
        }
    }
}

@Composable
fun BadgeItem(badge: Badge, adaptiveLearningManager: AdaptiveLearningManager) {
    val isAchieved = adaptiveLearningManager.isBadgeAchieved(badge.id)
    val awardedTime = adaptiveLearningManager.getBadgeAwardedTime(badge.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = badge.icon,
                contentDescription = badge.name,
                tint = if (isAchieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isAchieved && awardedTime != null) {
                    Text(
                        text = "Acquired: ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(awardedTime))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun TikTokPlayerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val tiktokVideos = listOf(
        TikTokVideo("How to Introduce Yourself", "https://www.tiktok.com/@yuto__jake/video/6999291080625753346?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Basic Conversations", "https://www.tiktok.com/@yuto__jake/video/6999464283193101570?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("How to Use 'Otsukaresama'", "https://www.tiktok.com/@yuto__jake/video/6999487398350507265?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Anime Language Introduction", "https://www.tiktok.com/@yuto__jake/video/7009137659687849218?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Anime Language Introduction 2", "https://www.tiktok.com/@yuto__jake/video/7009174948996533505?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Anime Language Introduction 3", "https://www.tiktok.com/@yuto__jake/video/7009893508521741570?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Anime Language Introduction 4", "https://www.tiktok.com/@yuto__jake/video/7010285580080696577?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Anime Language Introduction 5", "https://www.tiktok.com/@yuto__jake/video/7011425241654103322?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Anime Language Introduction 6", "https://www.tiktok.com/@yuto__jake/video/7012553283160083739?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Anime Language Introduction 7", "https://www.tiktok.com/@yuto__jake/video/7013214530486504730?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Japanese Vocabulary Introduction 1", "https://www.tiktok.com/@yuto__jake/video/7026910577020701978?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Japanese Vocabulary Introduction 2", "https://www.tiktok.com/@yuto__jake/video/7033286125946998043?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Meaning of 'Hisashiburi'", "https://www.tiktok.com/@yuto__jake/video/7056614114692451610?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Meaning of 'Wakarimashita'", "https://www.tiktok.com/@yuto__jake/video/7056781910629993729?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Meaning of 'Yamete'", "https://www.tiktok.com/@yuto__jake/video/7058181852992998682?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Meaning of 'Kore wa nan desu ka'", "https://www.tiktok.com/@yuto__jake/video/7058896124316355866?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Meaning of 'Wakaranai'", "https://www.tiktok.com/@yuto__jake/video/7059284943309933851?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Numbers", "https://www.tiktok.com/@yuto__jake/video/7061163706087951642?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Japanese Correction Video 1", "https://www.tiktok.com/@yuto__jake/video/7066667047769427226?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Japanese Correction Video 2", "https://www.tiktok.com/@yuto__jake/video/7070324425048001819?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Japanese Correction Video 3", "https://www.tiktok.com/@yuto__jake/video/7077008148225379610?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Japanese Correction Video 4", "https://www.tiktok.com/@yuto__jake/video/7079962091527998747?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Japanese Correction Video 5", "https://www.tiktok.com/@yuto__jake/video/7080049591898492186?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Japanese Correction Video 6", "https://www.tiktok.com/@yuto__jake/video/7095998374775737602?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Google Translate Correction Video", "https://www.tiktok.com/@yuto__jake/video/7098537135413890330?is_from_webapp=1&sender_device=pc"),
        TikTokVideo("Japanese Correction Video 7", "https://www.tiktok.com/@yuto__jake/video/7257856225403079937?is_from_webapp=1&sender_device=pc")
    )
    var selectedVideo by remember { mutableStateOf(tiktokVideos[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Learning Japanese with TikTok",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Select a video:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(tiktokVideos) { video ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedVideo = video }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedVideo == video,
                        onClick = { selectedVideo = video }
                    )
                    Text(
                        text = video.title,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedVideo.url))
                context.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Open Video")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to Main Menu")
        }
    }
}
data class TikTokVideo(val title: String, val url: String)