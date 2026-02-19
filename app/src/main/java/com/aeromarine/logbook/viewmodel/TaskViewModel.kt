package com.aeromarine.logbook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlin.random.Random

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    // LiveData для UI
    private val _games = MutableLiveData<List<Game>>(emptyList())
    val games: LiveData<List<Game>> = _games

    private val _currentGame = MutableLiveData<Game>(createNewGame())
    val currentGame: LiveData<Game> = _currentGame

    private val _statistics = MutableLiveData<Statistics>(Statistics())
    val statistics: LiveData<Statistics> = _statistics

    private val _goals = MutableLiveData<List<Goal>>(emptyList())
    val goals: LiveData<List<Goal>> = _goals

    private val _videoAnalyses = MutableLiveData<List<VideoAnalysis>>(emptyList())
    val videoAnalyses: LiveData<List<VideoAnalysis>> = _videoAnalyses

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // StateFlow для более реактивного UI
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    sealed class UiState {
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Загружаем демо данные
                loadDemoData()
                calculateStatistics()
                _uiState.value = UiState.Success("Data loaded")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load data: ${e.message}")
                _errorMessage.value = "Ошибка загрузки данных"
            }
        }
    }

    // === ИГРЫ ===
    fun createNewGame(): Game {
        return Game(frames = List(10) { Frame(number = it + 1) })
    }

    fun addRoll(frameIndex: Int, pins: Int, rollNumber: Int) {
        val currentGameValue = _currentGame.value ?: return
        val frames = currentGameValue.frames.toMutableList()

        if (frameIndex in frames.indices) {
            val frame = frames[frameIndex]
            val updatedFrame = when (rollNumber) {
                1 -> frame.copy(
                    roll1 = pins,
                    isStrike = pins == 10,
                    isSpare = false
                )
                2 -> {
                    val totalPins = frame.roll1 + pins
                    frame.copy(
                        roll2 = pins,
                        isStrike = false,
                        isSpare = totalPins == 10 && frame.roll1 != 10
                    )
                }
                3 -> frame.copy(roll3 = pins)
                else -> frame
            }

            frames[frameIndex] = updatedFrame
            val updatedGame = currentGameValue.copy(frames = frames)
            _currentGame.value = updatedGame

            // Автоматически пересчитываем счет
            calculateCurrentGameScore()
        }
    }

    private fun calculateCurrentGameScore() {
        val game = _currentGame.value ?: return
        val frames = game.frames
        var totalScore = 0
        val strikeCount = frames.count { it.isStrike }
        val spareCount = frames.count { it.isSpare }

        // Временное хранение счетов фреймов
        val frameScores = MutableList(10) { 0 }

        for (i in frames.indices) {
            val frame = frames[i]
            var frameScore = frame.roll1 + frame.roll2 + frame.roll3

            if (frame.isStrike && i < 9) {
                // Бонус за страйк
                val nextFrame = frames[i + 1]
                frameScore += nextFrame.roll1
                if (nextFrame.isStrike && i < 8) {
                    frameScore += frames[i + 2].roll1
                } else {
                    frameScore += nextFrame.roll2
                }
            } else if (frame.isSpare && i < 9) {
                // Бонус за спэр
                frameScore += frames[i + 1].roll1
            }

            frameScores[i] = frameScore
            totalScore += frameScore
        }

        // Обновляем счет каждого фрейма
        val updatedFrames = frames.mapIndexed { index, frame ->
            frame.copy(frameScore = frameScores[index])
        }

        val updatedGame = game.copy(
            frames = updatedFrames,
            totalScore = totalScore,
            strikeCount = strikeCount,
            spareCount = spareCount
        )
        _currentGame.value = updatedGame
    }

    fun saveCurrentGame() {
        val current = _currentGame.value ?: return
        if (current.frames.all { it.isComplete }) {
            val currentGames = _games.value ?: emptyList()
            val updatedGames = currentGames + current
            _games.value = updatedGames
            _currentGame.value = createNewGame()
            calculateStatistics()
            updateGoalsProgress()
            _errorMessage.value = "Игра сохранена! Счет: ${current.totalScore}"
        } else {
            _errorMessage.value = "Завершите все фреймы перед сохранением"
        }
    }

    fun clearCurrentGame() {
        _currentGame.value = createNewGame()
    }

    // === СТАТИСТИКА ===
    private fun calculateStatistics() {
        val gamesList = _games.value ?: emptyList()
        if (gamesList.isEmpty()) {
            _statistics.value = Statistics()
            return
        }

        val totalScore = gamesList.sumOf { it.totalScore }
        val averageScore = totalScore.toDouble() / gamesList.size
        val bestGame = gamesList.maxByOrNull { it.totalScore }?.totalScore ?: 0

        val totalStrikes = gamesList.sumOf { it.strikeCount }
        val totalSpares = gamesList.sumOf { it.spareCount }
        val totalFrames = gamesList.size * 10

        val strikePercentage = if (totalFrames > 0) (totalStrikes * 100.0) / totalFrames else 0.0
        val sparePercentage = if (totalFrames > 0) (totalSpares * 100.0) / totalFrames else 0.0

        val lastGames = gamesList.takeLast(5).map { it.totalScore }

        _statistics.value = Statistics(
            averageScore = averageScore,
            bestGame = bestGame,
            gamesPlayed = gamesList.size,
            strikePercentage = strikePercentage,
            sparePercentage = sparePercentage,
            lastGames = lastGames
        )
    }

    // === ЦЕЛИ ===
    fun addGoal(title: String, target: Double, type: GoalType) {
        val newGoal = Goal(title = title, target = target, type = type)
        val currentGoals = _goals.value ?: emptyList()
        val updatedGoals = currentGoals + newGoal
        _goals.value = updatedGoals as List<Goal>?
        _errorMessage.value = "Цель добавлена: $title"
    }

    private fun updateGoalsProgress() {
        val stats = _statistics.value ?: return
        val currentGoals = _goals.value ?: emptyList()

        val updatedGoals = currentGoals.map { goal ->
            val currentValue = when (goal.type) {
                GoalType.STRIKE_RATE -> stats.strikePercentage
                GoalType.SPARE_RATE -> stats.sparePercentage
                GoalType.AVERAGE_SCORE -> stats.averageScore
                GoalType.CONSECUTIVE_GAMES -> calculateConsecutiveGames()
                else -> {}
            }
            val isCompleted = currentValue as Double >= goal.target
            goal.copy(current = currentValue, isCompleted = isCompleted)
        }

        _goals.value = updatedGoals
    }

    private fun calculateConsecutiveGames(): Double {
        val gamesList = _games.value ?: emptyList()
        if (gamesList.size < 2) return 0.0

        var maxConsecutive = 0
        var currentConsecutive = 0
        var lastScore = 0

        gamesList.forEach { game ->
            if (game.totalScore >= 200) {
                if (game.totalScore >= lastScore) {
                    currentConsecutive++
                    maxConsecutive = maxOf(maxConsecutive, currentConsecutive)
                } else {
                    currentConsecutive = 1
                }
            } else {
                currentConsecutive = 0
            }
            lastScore = game.totalScore
        }

        return maxConsecutive.toDouble()
    }

    fun completeGoal(goalId: String) {
        val currentGoals = _goals.value ?: emptyList()
        val updatedGoals = currentGoals.map { goal ->
            if (goal.id == goalId) goal.copy(isCompleted = true) else goal
        }
        _goals.value = updatedGoals
    }

    fun deleteGoal(goalId: String) {
        val currentGoals = _goals.value ?: emptyList()
        val updatedGoals = currentGoals.filter { it.id != goalId }
        _goals.value = updatedGoals
        _errorMessage.value = "Цель удалена"
    }

    // === ВИДЕО АНАЛИЗ ===
    fun addVideoAnalysis(title: String, videoUri: String) {
        val newAnalysis = VideoAnalysis(title = title, videoUri = videoUri)
        val currentAnalyses = _videoAnalyses.value ?: emptyList()
        val updatedAnalyses = currentAnalyses + newAnalysis
        _videoAnalyses.value = updatedAnalyses
        _errorMessage.value = "Видео анализ добавлен: $title"
    }

    fun addVideoMarker(videoId: String, timeSeconds: Float, type: MarkerType, note: String) {
        val marker = VideoMarker(timeSeconds = timeSeconds, type = type, note = note)
        val currentAnalyses = _videoAnalyses.value ?: emptyList()
        val updatedAnalyses = currentAnalyses.map { analysis ->
            if (analysis.id == videoId) {
                analysis.copy(markers = analysis.markers + marker)
            } else {
                analysis
            }
        }
        _videoAnalyses.value = updatedAnalyses
        _errorMessage.value = "Маркер добавлен в видео"
    }

    fun deleteVideoAnalysis(videoId: String) {
        val currentAnalyses = _videoAnalyses.value ?: emptyList()
        val updatedAnalyses = currentAnalyses.filter { it.id != videoId }
        _videoAnalyses.value = updatedAnalyses
        _errorMessage.value = "Видео анализ удален"
    }

    // === ЭКСПОРТ ===
    fun exportStatisticsToPdf(): String {
        val stats = _statistics.value ?: return ""
        val gamesList = _games.value ?: emptyList()

        return buildString {
            appendLine("=== СТАТИСТИКА БОУЛИНГА ===")
            appendLine("Сгенерировано: ${Date()}")
            appendLine()
            appendLine("ОБЩАЯ СТАТИСТИКА:")
            appendLine("Средний счет: ${String.format("%.1f", stats.averageScore)}")
            appendLine("Лучшая игра: ${stats.bestGame}")
            appendLine("Сыграно игр: ${stats.gamesPlayed}")
            appendLine("Процент страйков: ${String.format("%.1f%%", stats.strikePercentage)}")
            appendLine("Процент спэаров: ${String.format("%.1f%%", stats.sparePercentage)}")
            appendLine()
            appendLine("ПОСЛЕДНИЕ ИГРЫ:")
            gamesList.takeLast(10).forEachIndexed { index, game ->
                appendLine("${index + 1}. ${game.totalScore} очков - ${game.date}")
            }
        }
    }

    // === УТИЛИТЫ ===
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun loadDemoData() {
        // Демо данные для тестирования
        val demoGames = listOf(
            Game(
                totalScore = 185,
                strikeCount = 4,
                spareCount = 3,
                frames = List(10) { i ->
                    Frame(
                        number = i + 1,
                        roll1 = if (i == 0) 10 else 7,
                        roll2 = if (i == 0) 0 else 2,
                        frameScore = when (i) {
                            0 -> 20
                            1 -> 15
                            else -> 9
                        }
                    )
                }
            ),
            Game(
                totalScore = 210,
                strikeCount = 6,
                spareCount = 2,
                frames = List(10) { i ->
                    Frame(
                        number = i + 1,
                        roll1 = if (i % 2 == 0) 10 else 8,
                        roll2 = if (i % 2 == 0) 0 else 2,
                        frameScore = when {
                            i % 2 == 0 -> 20
                            else -> 10
                        }
                    )
                }
            ),
            Game(
                totalScore = 168,
                strikeCount = 3,
                spareCount = 4,
                frames = List(10) { i ->
                    Frame(
                        number = i + 1,
                        roll1 = 6,
                        roll2 = 3,
                        frameScore = 9
                    )
                }
            )
        )
        _games.value = demoGames

        val demoGoals = listOf(
            Goal(
                id = "1",
                title = "Strike Rate 60%",
                target = 60.0,
                type = GoalType.STRIKE_RATE,
                current = 45.0
            ),
            Goal(
                id = "2",
                title = "Average 200+",
                target = 200.0,
                type = GoalType.AVERAGE_SCORE,
                current = 187.7
            ),
            Goal(
                id = "3",
                title = "Spare Rate 40%",
                target = 40.0,
                type = GoalType.SPARE_RATE,
                current = 30.0
            )
        )
        _goals.value = demoGoals

        val demoVideos = listOf(
            VideoAnalysis(
                id = "1",
                title = "Training Session 1",
                videoUri = "content://demo/video1",
                markers = listOf(
                    VideoMarker(5.0f, MarkerType.STRIKE, "Perfect strike form"),
                    VideoMarker(12.0f, MarkerType.ERROR, "Early release")
                )
            ),
            VideoAnalysis(
                id = "2",
                title = "Competition Analysis",
                videoUri = "content://demo/video2",
                markers = listOf(
                    VideoMarker(8.0f, MarkerType.TECHNIQUE, "Good footwork")
                )
            )
        )
        _videoAnalyses.value = demoVideos
    }

    // Быстрый ввод для демо
    fun addDemoGame() {
        val randomScore = Random.nextInt(120, 280)
        val randomStrikes = Random.nextInt(2, 8)
        val randomSpares = Random.nextInt(2, 6)

        val demoGame = Game(
            totalScore = randomScore,
            strikeCount = randomStrikes,
            spareCount = randomSpares,
            frames = List(10) { i ->
                Frame(
                    number = i + 1,
                    roll1 = Random.nextInt(0, 11),
                    roll2 = if (i < 9) Random.nextInt(0, 11) else 0,
                    frameScore = Random.nextInt(5, 30)
                )
            }
        )

        val currentGames = _games.value ?: emptyList()
        val updatedGames = currentGames + demoGame
        _games.value = updatedGames
        calculateStatistics()
        updateGoalsProgress()
        _errorMessage.value = "Демо игра добавлена: $randomScore очков"
    }
}

data class Game(
    val id: String = UUID.randomUUID().toString(),
    val date: Date = Date(),
    val frames: List<Frame> = emptyList(),
    val totalScore: Int = 0,
    val strikeCount: Int = 0,
    val spareCount: Int = 0
)

data class Frame(
    val number: Int,
    val roll1: Int = 0,
    val roll2: Int = 0,
    val roll3: Int = 0,
    val isStrike: Boolean = false,
    val isSpare: Boolean = false,
    val frameScore: Int = 0
) {
    val isComplete: Boolean
        get() = when {
            number < 10 -> isStrike || roll1 + roll2 == 10 || (roll1 > 0 && roll2 > 0)
            else -> (isStrike && roll2 > 0 && roll3 > 0) ||
                    (isSpare && roll3 > 0) ||
                    (roll1 + roll2 < 10)
        }
}

data class Statistics(
    val averageScore: Double = 0.0,
    val bestGame: Int = 0,
    val gamesPlayed: Int = 0,
    val strikePercentage: Double = 0.0,
    val sparePercentage: Double = 0.0,
    val lastGames: List<Int> = emptyList()
)

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val target: Double,
    val current: Double = 0.0,
    val type: GoalType,
    val isCompleted: Boolean = false
)

enum class GoalType {
    STRIKE_RATE, AVERAGE_SCORE, SPARE_RATE, CONSECUTIVE_GAMES
}

data class VideoAnalysis(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val videoUri: String,
    val markers: List<VideoMarker> = emptyList(),
    val date: Date = Date()
)

data class VideoMarker(
    val timeSeconds: Float,
    val type: MarkerType,
    val note: String
)

enum class MarkerType {
    STRIKE, SPARE, ERROR, TECHNIQUE
}
