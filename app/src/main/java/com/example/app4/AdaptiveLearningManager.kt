package com.example.app4

import android.content.Context
import org.json.JSONObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.LocalFireDepartment

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val condition: () -> Boolean,
    val icon: ImageVector
)

class AdaptiveLearningManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("adaptive_learning", Context.MODE_PRIVATE)
    private val words: List<Word> = loadWords(context)
    private val grammars: List<Grammar> = loadGrammars(context)

    private fun getCurrentUser(): String {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("current_user", "") ?: ""
    }

    private fun getKey(key: String): String {
        val currentUser = getCurrentUser()
        return "${currentUser}_$key"
    }

    fun setCurrentWord(word: String) {
        sharedPreferences.edit().putString(getKey("current_word"), word).apply()
    }

    fun getCurrentWord(): String? {
        return sharedPreferences.getString(getKey("current_word"), null)
    }

    // お気に入り管理
    fun toggleFavorite(item: String, type: String) {
        val favorites = getFavorites(type).toMutableSet()
        if (favorites.contains(item)) {
            favorites.remove(item)
        } else {
            favorites.add(item)
        }
        saveFavorites(favorites, type)
    }

    fun isFavorite(item: String, type: String): Boolean {
        return getFavorites(type).contains(item)
    }

    fun getFavorites(type: String): Set<String> {
        return sharedPreferences.getStringSet(getKey("${type}_favorites"), emptySet()) ?: emptySet()
    }

    fun getFavoriteWords(): List<String> {
        return getFavorites("word").toList()
    }

    fun getFavoriteGrammars(): List<String> {
        return getFavorites("grammar").toList()
    }

    private fun saveFavorites(favorites: Set<String>, type: String) {
        sharedPreferences.edit().putStringSet(getKey("${type}_favorites"), favorites).apply()
    }

    fun saveWordStudyProgress(index: Int) {
        sharedPreferences.edit().putInt("word_study_progress", index).apply()
    }

    fun getWordStudyProgress(): Int {
        return sharedPreferences.getInt("word_study_progress", 0)
    }

    fun loadWords(context: Context): List<Word> {
        val inputStream = context.assets.open("words.json")
        val reader = InputStreamReader(inputStream)
        val wordType = object : TypeToken<List<Word>>() {}.type
        return Gson().fromJson(reader, wordType)



    }

    fun loadGrammars(context: Context): List<Grammar> {
        val inputStream = context.assets.open("grammar.json")
        val reader = InputStreamReader(inputStream)
        val grammarType = object : TypeToken<List<Grammar>>() {}.type
        return Gson().fromJson(reader, grammarType)
    }

    fun getWordIndex(word: String): Int {
        return words.indexOfFirst { it.word == word }
    }

    fun getGrammarIndex(grammar: String): Int {
        return grammars.indexOfFirst { it.grammar == grammar }
    }

    fun saveStudyProgress(type: String, progress: Int) {
        sharedPreferences.edit().putInt(getKey("${type}_progress"), progress).apply()
    }

    fun getStudyProgress(type: String): Int {
        return sharedPreferences.getInt(getKey("${type}_progress"), 0)
    }

    fun updateLearningProgress(type: String, progress: Double) {
        sharedPreferences.edit().putFloat(getKey("${type}_learning_progress"), progress.toFloat()).apply()
    }

    fun getLearningProgress(type: String): Double {
        return sharedPreferences.getFloat(getKey("${type}_learning_progress"), 0f).toDouble()
    }

    fun getRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()

        val weakWords = getWeakItems("word").take(3)
        val weakGrammars = getWeakItems("grammar").take(3)

        val wordProgress = getLearningProgress("word")
        val grammarProgress = getLearningProgress("grammar")

        if (wordProgress < 0.5) {
            recommendations.add("Keep learning words. Current Progress: ${(wordProgress * 100).toInt()}%")
        }

        if (grammarProgress < 0.5) {
            recommendations.add("Keep learning grammars. Current Progress: ${(grammarProgress * 100).toInt()}%")
        }

        if (weakWords.isNotEmpty()) {
            recommendations.add("Review the following words: ${weakWords.joinToString(", ") { it.first }}")
        }

        if (weakGrammars.isNotEmpty()) {
            recommendations.add("Review the following grammars: ${weakGrammars.joinToString(", ") { it.first }}")
        }
        return recommendations
    }

    fun saveBadgeStatus(badgeId: String, achieved: Boolean) {
        sharedPreferences.edit().putBoolean(getKey("badge_$badgeId"), achieved).apply()
    }

    fun getQuizQuestionsByDifficulty(allQuestions: List<QuizQuestion>, difficulty: String): List<QuizQuestion> {
        return allQuestions.filter { it.difficulty == difficulty }.shuffled()
    }

    fun updateHighScore(difficulty: String, score: Int) {
        val key = getKey("high_score_$difficulty")
        val currentHighScore = sharedPreferences.getInt(key, 0)
        if (score > currentHighScore) {
            sharedPreferences.edit().putInt(key, score).apply()
        }
    }

    fun getHighScore(difficulty: String): Int {
        return sharedPreferences.getInt(getKey("high_score_$difficulty"), 0)
    }

    fun updateCorrectRate(type: String, correctRate: Float) {
        val key = getKey("${type}_correct_rate")
        val currentData = JSONObject(sharedPreferences.getString(key, "{\"correct\":0,\"total\":0}") ?: "{\"correct\":0,\"total\":0}")
        val total = currentData.getInt("total")
        val newCorrect = (correctRate * (total + 1)).toInt()
        val newData = JSONObject().apply {
            put("correct", newCorrect)
            put("total", total + 1)
        }
        sharedPreferences.edit().putString(key, newData.toString()).apply()
    }

    fun getCorrectRate(type: String): Float {
        val key = getKey("${type}_correct_rate")
        val data = JSONObject(sharedPreferences.getString(key, "{\"correct\":0,\"total\":0}") ?: "{\"correct\":0,\"total\":0}")
        val correct = data.getInt("correct")
        val total = data.getInt("total")
        return if (total > 0) correct.toFloat() / total else 0f
    }

    fun getWeakItems(type: String): List<Pair<String, Double>> {
        return sharedPreferences.all
            .filter { it.key.startsWith(getKey("${type}_correct_rate_")) }
            .map { (key, value) ->
                val item = key.removePrefix(getKey("${type}_correct_rate_"))
                val data = JSONObject(value as String)
                val correct = data.getInt("correct")
                val total = data.getInt("total")
                val rate = if (total > 0) correct.toDouble() / total else 0.0
                Pair(item, rate)
            }
            .sortedBy { it.second }
            .take(5)
    }

    fun scheduleReview(item: String, type: String, isCorrect: Boolean) {
        val key = getKey("${type}_review_schedule_$item")
        val currentTime = System.currentTimeMillis()
        val nextReview = if (isCorrect) {
            currentTime + calculateNextInterval(item, type)
        } else {
            currentTime + DAY_IN_MILLIS
        }
        sharedPreferences.edit().putLong(key, nextReview).apply()
    }

    fun getItemsDueForReview(type: String): List<String> {
        val currentTime = System.currentTimeMillis()
        return sharedPreferences.all
            .filter { it.key.startsWith(getKey("${type}_review_schedule_")) && it.value as Long <= currentTime }
            .map { it.key.removePrefix(getKey("${type}_review_schedule_")) }
    }

    fun incrementQuizAttemptCount(difficulty: String) {
        val key = getKey("quiz_attempt_count_$difficulty")
        val currentCount = sharedPreferences.getInt(key, 0)
        sharedPreferences.edit().putInt(key, currentCount + 1).apply()
    }

    fun getQuizAttemptCount(difficulty: String): Int {
        val key = getKey("quiz_attempt_count_$difficulty")
        return sharedPreferences.getInt(key, 0)
    }

    private fun calculateNextInterval(item: String, type: String): Long {
        val key = getKey("${type}_review_interval_$item")
        val currentInterval = sharedPreferences.getLong(key, DAY_IN_MILLIS)
        val nextInterval = (currentInterval * 2).coerceAtMost(MAX_INTERVAL)
        sharedPreferences.edit().putLong(key, nextInterval).apply()
        return nextInterval
    }

    fun getSortedWords(words: List<Word>): List<Word> {
        val weakItems = getWeakItems("word").map { it.first }
        return words.sortedWith(compareBy(
            { weakItems.indexOf(it.word) },
            { !isFavorite(it.word, "word") }
        ))
    }

    fun recordMistake(questionId: String) {
        val key = getKey("mistake_count_$questionId")
        val currentMistakes = sharedPreferences.getInt(key, 0)
        val newMistakeCount = currentMistakes + 1
        sharedPreferences.edit().putInt(key, newMistakeCount).apply()

        if (newMistakeCount >= 3) {
            addToReviewList(questionId)
        }
    }

    private fun addToReviewList(questionId: String) {
        val reviewListKey = getKey("review_list")
        val currentReviewList = sharedPreferences.getStringSet(reviewListKey, mutableSetOf()) ?: mutableSetOf()
        currentReviewList.add(questionId)
        sharedPreferences.edit().putStringSet(reviewListKey, currentReviewList).apply()
    }

    fun getReviewList(): Set<String> {
        val reviewListKey = getKey("review_list")
        return sharedPreferences.getStringSet(reviewListKey, mutableSetOf()) ?: mutableSetOf()
    }

    fun removeFromReviewList(questionId: String) {
        val reviewListKey = getKey("review_list")
        val currentReviewList = sharedPreferences.getStringSet(reviewListKey, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        currentReviewList.remove(questionId)
        sharedPreferences.edit().putStringSet(reviewListKey, currentReviewList).apply()
    }

    fun recordLearningActivity(type: String, duration: Long) {
        val key = getKey("${type}_learning_time")
        val currentTime = sharedPreferences.getLong(key, 0L)
        sharedPreferences.edit().putLong(key, currentTime + duration).apply()
        updateLearningStreak()
    }


    fun updateLearningStreak() {
        val today = Calendar.getInstance().also { calendar ->
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val lastLearningDay = sharedPreferences.getLong(getKey("last_learning_day"), 0L)
        val currentStreak = sharedPreferences.getInt(getKey("learning_streak"), 0)

        if (today - lastLearningDay > 24 * 60 * 60 * 1000) {
            sharedPreferences.edit().putInt(getKey("learning_streak"), 1).apply()
        } else if (today > lastLearningDay) {
            sharedPreferences.edit().putInt(getKey("learning_streak"), currentStreak + 1).apply()
        }
        sharedPreferences.edit().putLong(getKey("last_learning_day"), today).apply()
    }

    fun getLearningStreak(): Int {
        return sharedPreferences.getInt(getKey("learning_streak"), 0)
    }

    fun getLearningTime(type: String): Long {
        return sharedPreferences.getLong(getKey("${type}_learning_time"), 0L)
    }

    private val badges = listOf(
        Badge(
            "streak_7",
            "7-day streak",
            "7 consecutive days of study",
            { getLearningStreak() >= 7 },
            Icons.Default.Whatshot
        ),
        Badge(
            "streak_30",
            "30-day streak",
            "30 consecutive days of study",
            { getLearningStreak() >= 30 },
            Icons.Default.LocalFireDepartment
        ),
        Badge(
            "total_time_10h",
            "10 hours of study time",
            "Total study time exceeds 10 hours",
            { (getLearningTime("word") + getLearningTime("grammar")) / (60 * 60 * 1000) >= 10 },
            Icons.Default.Timer
        ),
        Badge(
            "quiz_master_easy",
            "Quiz Master (word)",
            "Achieve 90% or more correct answers on word quizzes",
            { getCorrectRate("word") >= 0.9f },
            Icons.Default.EmojiEvents
        ),
        Badge(
            "quiz_master_hard",
            "Quiz Master (grammar)",
            "Achieve 90% or more correct answers on grammar quizzes",
            { getCorrectRate("grammar") >= 0.9f },
            Icons.Default.MilitaryTech
        ),
        Badge(
            "perfect_quiz_easy",
            "Perfect score (Word)",
            "Get a perfect score on all word quiz questions",
            { getHighScore("easy") == 98 },
            Icons.Default.Grade
        ),
        Badge(
            "perfect_quiz_hard",
            "Perfect score (Grammar)",
            "Get a perfect score on all grammar quiz questions",
            { getHighScore("hard") == 96 },
            Icons.Default.Stars
        )
    )

    fun getBadges(): List<Badge> {
        return badges
    }

    fun checkAndAwardBadges() {
        for (badge in badges) {
            if (badge.condition() && !isBadgeAchieved(badge.id)) {
                awardBadge(badge.id)
            }
        }
    }

    fun isBadgeAchieved(badgeId: String): Boolean {
        return sharedPreferences.getBoolean(getKey("badge_$badgeId"), false)
    }

    fun awardBadge(badgeId: String) {
        sharedPreferences.edit()
            .putBoolean(getKey("badge_$badgeId"), true)
            .putLong(getKey("badge_${badgeId}_awarded_at"), System.currentTimeMillis())
            .apply()
    }

    fun getBadgeAwardedTime(badgeId: String): Long? {
        val time = sharedPreferences.getLong(getKey("badge_${badgeId}_awarded_at"), -1)
        return if (time != -1L) time else null
    }

    companion object {
        const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        const val MAX_INTERVAL = 30 * DAY_IN_MILLIS
    }
}