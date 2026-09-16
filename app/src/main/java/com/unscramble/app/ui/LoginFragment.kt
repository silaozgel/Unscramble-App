package com.unscramble.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.unscramble.app.R
import com.unscramble.app.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Eğer kullanıcı zaten giriş yapmışsa direkt Ana Sayfaya yönlendirir
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Her tıklamada önceki hataları temizler
            binding.emailLayout.error = null
            binding.passwordLayout.error = null

            var isValid = true

            // E-posta Doğrulama (Regex ile uzantı kontrolü)
            // Sadece gmail.com, hotmail.com vb uzantılarına izin verir
            val emailPattern = "^[A-Za-z0-9+_.-]+@(gmail\\.com|hotmail\\.com|outlook\\.com|yahoo\\.com|yandex\\.com)$"
            if (!email.matches(Regex(emailPattern))) {
                binding.emailLayout.error = "Lütfen geçerli bir e-posta girin (@gmail.com, @hotmail.com vb.)"
                isValid = false
            }

            // Şifre Doğrulama (En az 6 karakter)
            if (password.length < 6) {
                binding.passwordLayout.error = "Şifre en az 6 karakter olmalıdır"
                isValid = false
            }

            // Eğer formatlar doğruysa Firebase ile girişi dener
            if (isValid) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        } else {
                            // Firebase'den dönen hataya göre ilgili alanda uyarı gösterir
                            val exception = task.exception
                            when (exception) {
                                is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                                    binding.emailLayout.error = "Bu e-posta ile kayıtlı bir hesap bulunamadı"
                                }
                                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                    binding.passwordLayout.error = "Şifreniz hatalı, lütfen tekrar deneyin"
                                }
                                else -> {
                                    binding.passwordLayout.error = "Giriş başarısız oldu"
                                }
                            }
                        }
                    }
            }
        }

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Bellek sızıntısını önlemek için
    }
}