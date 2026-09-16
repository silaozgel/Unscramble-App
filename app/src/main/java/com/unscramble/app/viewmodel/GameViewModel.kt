package com.unscramble.app.viewmodel

import androidx.lifecycle.ViewModel
import com.unscramble.app.model.GameData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GameUiState(
    val currentScrambledWord: String = "",
    val currentWordCount: Int = 1,
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val lives: Int = 5,
    val difficulty: String = "Kolay",
    val isEnglish: Boolean = false
)

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentWord: String = ""
    private var usedWords: MutableSet<String> = mutableSetOf()
    private val maxNoOfWords = 20

    init {
        resetGame()
    }

    private fun pickRandomWordAndShuffle(): String {
        val wordListMap = if (_uiState.value.isEnglish) GameData.wordsEn else GameData.wordsTr

        // 1-7 Kolay, 8-14 Orta, 15-20 Zor
        val difficultyKey = when (_uiState.value.currentWordCount) {
            in 1..7 -> "kolay"
            in 8..14 -> "orta"
            else -> "zor"
        }

        val currentList = wordListMap[difficultyKey] ?: listOf("hata")

        // Kullanılmamış bir kelime bulur
        currentWord = currentList.random()
        while (usedWords.contains(currentWord)) {
            currentWord = currentList.random()
        }
        usedWords.add(currentWord)

        val tempWord = currentWord.toCharArray()
        tempWord.shuffle()
        while (String(tempWord) == currentWord) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(
            currentWordCount = 1,
            score = 0,
            lives = 5,
            isGameOver = false,
            difficulty = "Kolay",
            isEnglish = _uiState.value.isEnglish
        )
        _uiState.update { currentState ->
            currentState.copy(currentScrambledWord = pickRandomWordAndShuffle())
        }
    }

    fun setLanguage(isEnglish: Boolean) {
        if (_uiState.value.isEnglish != isEnglish) {
            _uiState.update { currentState -> currentState.copy(isEnglish = isEnglish) }
            resetGame() // Dil değişince oyunu sıfırlar
        }
    }

    fun checkUserGuess(userGuess: String): Boolean {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val points = when (_uiState.value.difficulty) {
                "Kolay" -> 10
                "Orta" -> 20
                "Zor" -> 30
                else -> 10
            }
            updateGameState(points)
            return true
        }
        return false
    }

    fun skipWord() {
        val currentLives = _uiState.value.lives - 1

        if (currentLives <= 0) {
            _uiState.update { currentState ->
                currentState.copy(lives = 0, isGameOver = true)
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(lives = currentLives)
            }
            updateGameState(0) // Skor artmaz, sadece kelime değişir
        }
    }

    private fun updateGameState(updatedScore: Int) {
        if (_uiState.value.currentWordCount == maxNoOfWords) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGameOver = true,
                    score = currentState.score + updatedScore
                )
            }
        } else {
            val nextWordCount = _uiState.value.currentWordCount + 1
            val nextDifficulty = when (nextWordCount) {
                in 1..7 -> "Kolay"
                in 8..14 -> "Orta"
                else -> "Zor"
            }

            _uiState.update { currentState ->
                currentState.copy(
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    currentWordCount = nextWordCount,
                    score = currentState.score + updatedScore,
                    difficulty = nextDifficulty
                )
            }
        }
    }
}