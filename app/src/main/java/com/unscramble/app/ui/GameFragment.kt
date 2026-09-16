package com.unscramble.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unscramble.app.databinding.FragmentGameBinding
import com.unscramble.app.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Oyun açıldığında önce dil sorar (Türkçe/İngilizce)
        showLanguageSelectionDialog()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                if (uiState.isGameOver) {
                    updateHighScore(uiState.score)
                    showFinalScoreDialog(uiState.score)
                } else {
                    binding.tvScrambledWord.text = uiState.currentScrambledWord
                    binding.tvWordCount.text = "${uiState.currentWordCount} / 20"
                    binding.tvScore.text = "Skor: ${uiState.score}"

                    updateHeartsUI(uiState.lives)
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            val userGuess = binding.etGuess.text.toString().trim()
            if (userGuess.isNotEmpty()) {
                if (viewModel.checkUserGuess(userGuess)) {
                    binding.textFieldGuess.error = null
                    binding.etGuess.text?.clear()
                } else {
                    binding.textFieldGuess.error = "Yanlış kelime, tekrar dene!"
                }
            }
        }

        binding.btnSkip.setOnClickListener {
            viewModel.skipWord()
            binding.etGuess.text?.clear()
            binding.textFieldGuess.error = null
        }
    }

    // Dil seçimi için uyarı penceresi
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("Türkçe", "English")
        AlertDialog.Builder(requireContext())
            .setTitle("Dil Seçin / Select Language")
            .setCancelable(false) // Seçim yapmadan kapatmayı engeller
            .setItems(languages) { _, which ->
                val isEnglish = which == 1
                viewModel.setLanguage(isEnglish)
            }
            .show()
    }

    private fun updateHeartsUI(lives: Int) {
        val hearts = arrayOf(
            binding.heart1, binding.heart2, binding.heart3,
            binding.heart4, binding.heart5
        )

        for (i in hearts.indices) {
            if (i < lives) {
                hearts[i].visibility = View.VISIBLE
            } else {
                hearts[i].visibility = View.INVISIBLE // Kaybedilen canları gizler
            }
        }
    }

    private fun showFinalScoreDialog(score: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(if (viewModel.uiState.value.lives == 0) "Canınız Bitti!" else "Tebrikler!")
            .setMessage("Oyun bitti. Skorunuz: $score")
            .setCancelable(false)
            .setPositiveButton("Yeniden Oyna") { _, _ ->
                viewModel.resetGame()
                binding.etGuess.text?.clear()
            }
            .setNegativeButton("Çıkış") { _, _ ->
                findNavController().popBackStack()
            }
            .show()
    }

    private fun updateHighScore(newScore: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentHighScore = document.getLong("score")?.toInt() ?: 0
                if (newScore > currentHighScore) {
                    userRef.update("score", newScore)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}