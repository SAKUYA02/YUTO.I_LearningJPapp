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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder

data class Grammar(val grammar: String, val example: String)

@Composable
fun GrammarStudyScreen(navController: NavHostController, context: Context, adaptiveLearningManager: AdaptiveLearningManager) {
    val grammars = loadGrammars(context)
    var currentIndex by remember { mutableStateOf(adaptiveLearningManager.getStudyProgress("grammar")) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    DisposableEffect(Unit) {
        startTime = System.currentTimeMillis()
        onDispose {
            val duration = System.currentTimeMillis() - startTime
            adaptiveLearningManager.recordLearningActivity("grammar", duration)
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
                    text = "Grammar Study",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                if (currentIndex < grammars.size) {
                    val grammar = grammars[currentIndex]
                    val grammarKey = "grammar_${grammar.grammar}"
                    Text(
                        text = "Grammar ${currentIndex + 1} / ${grammars.size}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Grammar: ${grammar.grammar}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Example: ${grammar.example}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(
                        onClick = { adaptiveLearningManager.toggleFavorite(grammarKey, "grammar") }
                    ) {
                        Icon(
                            imageVector = if (adaptiveLearningManager.isFavorite(grammarKey, "grammar"))
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
                                    adaptiveLearningManager.saveStudyProgress("grammar", currentIndex)
                                }
                            },
                            enabled = currentIndex > 0
                        ) {
                            Text("Previous")
                        }
                        Button(
                            onClick = {
                                if (currentIndex < grammars.size - 1) {
                                    currentIndex++
                                    adaptiveLearningManager.saveStudyProgress("grammar", currentIndex)
                                    adaptiveLearningManager.updateLearningProgress("grammar", currentIndex.toDouble() / grammars.size)
                                }
                            },
                            enabled = currentIndex < grammars.size - 1
                        ) {
                            Text("Next")
                        }
                    }
                } else {
                    Text(
                        text = "All grammars have been studied!",
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
fun loadGrammars(context: Context): List<Grammar> {
    var grammars by remember { mutableStateOf(emptyList<Grammar>()) }

    LaunchedEffect(Unit) {
        grammars = withContext(Dispatchers.IO) {
            loadGrammarJsonData(context, "grammar.json")
        }
    }

    return grammars
}

suspend fun loadGrammarJsonData(context: Context, fileName: String): List<Grammar> {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return emptyList()
    }

    val jsonArray = JSONArray(jsonString)
    val grammars = mutableListOf<Grammar>()
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val grammar = jsonObject.getString("grammar")
        val example = jsonObject.getString("example")
        grammars.add(Grammar(grammar, example))
    }

    return grammars
}
