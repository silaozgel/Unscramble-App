package com.unscramble.app.viewmodel

import androidx.lifecycle.ViewModel
import com.unscramble.app.model.FlagData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class FlagUiState(
    val currentFlagResId: Int = 0,
    val currentCountryName: String = "",
    val currentLevel: Int = 1,
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val lives: Int = 5,
    val difficulty: String = "Kolay"
)

class FlagGameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FlagUiState())
    val uiState: StateFlow<FlagUiState> = _uiState.asStateFlow()

    private var usedCountries: MutableSet<String> = mutableSetOf()
    private val maxLevels = 20

    init {
        resetGame()
    }

    private fun pickRandomFlag(): Pair<String, Int> {
        val difficultyKey = when (_uiState.value.currentLevel) {
            in 1..7 -> FlagData.easyFlags
            in 8..14 -> FlagData.mediumFlags
            else -> FlagData.hardFlags
        }

        var randomEntry = difficultyKey.entries.random()
        // Rastgele seçer
        while (usedCountries.contains(randomEntry.key)) {
            // Eğer bir zorluktaki tüm bayraklar bittiyse hata vermemesi için
            if (usedCountries.containsAll(difficultyKey.keys)) break
            randomEntry = difficultyKey.entries.random()
        }

        usedCountries.add(randomEntry.key)
        return Pair(randomEntry.key, randomEntry.value)
    }

    fun resetGame() {
        usedCountries.clear()
        _uiState.value = FlagUiState(
            currentLevel = 1,
            score = 0,
            lives = 5,
            isGameOver = false,
            difficulty = "Kolay"
        )
        setNextFlag()
    }

    private fun setNextFlag() {
        val nextFlag = pickRandomFlag()
        _uiState.update { currentState ->
            currentState.copy(
                currentCountryName = nextFlag.first,
                currentFlagResId = nextFlag.second
            )
        }
    }

    private fun normalizeText(text: String): String {
        val localeTr = Locale("tr", "TR")
        return text.lowercase(localeTr)
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u")
    }

    fun checkUserGuess(userGuess: String): Boolean {
        // Türkçe karakter (İ, ı) sorunlarını çözmek için Türkçe dil kuralı tanımlandı
        val localeTr = Locale("tr", "TR")

        val guess = normalizeText(userGuess.trim())
        val answer = normalizeText(_uiState.value.currentCountryName)

        if (guess == answer) {
            // Doğru tahmin
            val points = when (_uiState.value.difficulty) {
                "Kolay" -> 10
                "Orta" -> 20
                "Zor" -> 30
                else -> 10
            }
            updateGameState(points)
            return true
        } else {
            // Yanlış tahmin durumu
            val currentLives = _uiState.value.lives - 1

            _uiState.update { currentState ->
                currentState.copy(lives = currentLives)
            }
            return false
        }
    }

    fun moveToNextFlag() {
        if (_uiState.value.lives <= 0) {
            _uiState.update { currentState ->
                currentState.copy(isGameOver = true)
            }
        } else {
            // Can varsa puan eklemeden yeni soruya geçer
            updateGameState(0)
        }
    }

    fun skipFlag() {
        val currentLives = _uiState.value.lives - 1

        if (currentLives <= 0) {
            _uiState.update { currentState ->
                currentState.copy(lives = 0, isGameOver = true)
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(lives = currentLives)
            }
            updateGameState(0) // Puan vermeden atlar
        }
    }

    private fun updateGameState(updatedScore: Int) {
        if (_uiState.value.currentLevel == maxLevels) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGameOver = true,
                    score = currentState.score + updatedScore
                )
            }
        } else {
            val nextLevel = _uiState.value.currentLevel + 1
            val nextDifficulty = when (nextLevel) {
                in 1..7 -> "Kolay"
                in 8..14 -> "Orta"
                else -> "Zor"
            }

            _uiState.update { currentState ->
                currentState.copy(
                    currentLevel = nextLevel,
                    score = currentState.score + updatedScore,
                    difficulty = nextDifficulty
                )
            }
            setNextFlag()
        }
    }

}