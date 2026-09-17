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
import com.unscramble.app.databinding.FragmentFlagGameBinding
import com.unscramble.app.viewmodel.FlagGameViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class FlagGameFragment : Fragment() {

    private var _binding: FragmentFlagGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FlagGameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlagGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                if (uiState.isGameOver) {
                    updateHighScore(uiState.score)
                    showFinalScoreDialog(uiState.score)
                } else {
                    // Bayrak resmini günceller
                    if (uiState.currentFlagResId != 0) {
                        binding.ivFlag.setImageResource(uiState.currentFlagResId)
                    }

                    binding.tvWordCount.text = "Seviye ${uiState.currentLevel}/20 - ${uiState.difficulty}"
                    binding.tvScore.text = "Skor: ${uiState.score}"
                    updateHeartsUI(uiState.lives)
                }
            }
        }
        binding.btnSubmit.setOnClickListener {
            val guess = binding.etGuess.text.toString()
            val correctAnswer = viewModel.uiState.value.currentCountryName

            val isCorrect = viewModel.checkUserGuess(guess)

            if (isCorrect) {
                binding.etGuess.text?.clear()
                binding.textFieldGuess.error = null
            } else {
                // Tahmin yanlışsa butonları kilitler ve doğru cevabı gösterir
                binding.btnSkip.isEnabled = false
                binding.btnSubmit.isEnabled = false
                binding.etGuess.isEnabled = false

                binding.textFieldGuess.error = "Yanlış! Doğru Cevap: $correctAnswer"

                // 3 saniye boyunca doğru kelimeyi gösterip sonraki aşamaya geçer
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(3000) // 3 saniye bekle

                    viewModel.moveToNextFlag()

                    // Ekranı yeni soru için temizler ve kilitleri açar
                    binding.etGuess.text?.clear()
                    binding.textFieldGuess.error = null
                    binding.btnSkip.isEnabled = true
                    binding.btnSubmit.isEnabled = true
                    binding.etGuess.isEnabled = true
                }
            }
        }

        binding.btnSkip.setOnClickListener {
            // Kullanıcı Geç butonuna basarsa doğru cevabı 3 saniye boyunca gösterir, sonra otomatik sıradakine geçer
            val correctAnswer = viewModel.uiState.value.currentCountryName

            // 3 saniye boyunca spam tıklamaları önlemek için tüm girdileri kilitler
            binding.btnSkip.isEnabled = false
            binding.btnSubmit.isEnabled = false
            binding.etGuess.isEnabled = false

            // Doğru cevabı yazı alanının altında gösterir
            binding.textFieldGuess.error = "Doğru Cevap: $correctAnswer"

            // Ekran kapatılırsa sayaç güvenli şekilde iptal olur
            viewLifecycleOwner.lifecycleScope.launch {
                delay(3000) // 3 saniye bekler

                // 3 saniye bittikten sonra normal atlama işlemini gerçekleştir (can -1)
                viewModel.skipFlag()

                // Ekranı yeni soru için temizler ve kilitleri açar
                binding.etGuess.text?.clear()
                binding.textFieldGuess.error = null
                binding.btnSkip.isEnabled = true
                binding.btnSubmit.isEnabled = true
                binding.etGuess.isEnabled = true
            }
        }
    }

    private fun updateHeartsUI(lives: Int) {
        val hearts: List<ImageView> = listOf(
            binding.heart1, binding.heart2, binding.heart3,
            binding.heart4, binding.heart5
        )
        for (i in hearts.indices) {
            hearts[i].visibility = if (i < lives) View.VISIBLE else View.INVISIBLE
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
                // Diğer oyunun skoruyla karışmaması için 'flagScore' alanını kullanıyoruz
                val currentHighScore = document.getLong("flagScore")?.toInt() ?: 0
                if (newScore > currentHighScore) {
                    userRef.update("flagScore", newScore)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
