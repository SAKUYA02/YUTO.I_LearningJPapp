package com.example.app4

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import java.util.*

data class Word(val word: String, val meaning: String, val reading: String)

@Composable
fun WordStudyScreen(navController: NavHostController, context: Context, adaptiveLearningManager: AdaptiveLearningManager) {
    val words = loadWords(context)
    var currentIndex by remember { mutableStateOf(adaptiveLearningManager.getStudyProgress("word")) }
    var showMeaning by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.JAPANESE
            }
        }
        startTime = System.currentTimeMillis()
        onDispose {
            val duration = System.currentTimeMillis() - startTime
            adaptiveLearningManager.recordLearningActivity("word", duration)
            tts.value?.stop()
            tts.value?.shutdown()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Word Study",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                if (currentIndex < words.size) {
                    val word = words[currentIndex]
                    Text(
                        text = "Word ${currentIndex + 1} / ${words.size}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = "Reading: ${word.reading}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            tts.value?.speak(word.word, TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Listen")
                    }
                    if (showMeaning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Meaning: ${word.meaning}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showMeaning = !showMeaning },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(if (showMeaning) "Hide meaning" else "Show meaning")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(
                        onClick = { adaptiveLearningManager.toggleFavorite(word.word, "word") }
                    ) {
                        Icon(
                            imageVector = if (adaptiveLearningManager.isFavorite(word.word, "word"))
                                Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite"
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                if (currentIndex > 0) {
                                    currentIndex--
                                    showMeaning = false
                                    adaptiveLearningManager.saveStudyProgress("word", currentIndex)
                                }
                            },
                            enabled = currentIndex > 0
                        ) {
                            Text("Previous")
                        }
                        Button(
                            onClick = {
                                if (currentIndex < words.size - 1) {
                                    currentIndex++
                                    showMeaning = false
                                    adaptiveLearningManager.saveStudyProgress("word", currentIndex)
                                    adaptiveLearningManager.updateLearningProgress("word", currentIndex.toDouble() / words.size)
                                }
                            },
                            enabled = currentIndex < words.size - 1
                        ) {
                            Text("Next")
                        }
                    }
                } else {
                    Text(
                        text = "All words have been studied!",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Back to Main Menu")
                }
            }
        }
    )
}

@Composable
fun loadWords(context: Context): List<Word> {
    var words by remember { mutableStateOf(emptyList<Word>()) }

    LaunchedEffect(Unit) {
        words = withContext(Dispatchers.IO) {
            loadWordJsonData(context, "words.json")
        }
    }
    return words
}

suspend fun loadWordJsonData(context: Context, fileName: String): List<Word> {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return emptyList()
    }

    val jsonArray = JSONArray(jsonString)
    val words = mutableListOf<Word>()
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val word = jsonObject.getString("word")
        val meaning = jsonObject.getString("meaning")
        val reading = jsonObject.getString("reading")
        words.add(Word(word, meaning, reading))
    }

    return words
}
