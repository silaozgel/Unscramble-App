package com.unscramble.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.unscramble.app.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Firebase servislerini başlatır
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Her tıklamada önceki hataları temizler
            binding.usernameLayout.error = null
            binding.emailLayout.error = null
            binding.passwordLayout.error = null

            var isValid = true

            // Kullanıcı adı kontrolü
            if (username.isEmpty()) {
                binding.usernameLayout.error = "Kullanıcı adı boş bırakılamaz"
                isValid = false
            }

            // E-posta Doğrulama (Sadece belirli uzantılar)
            val emailPattern = "^[A-Za-z0-9+_.-]+@(gmail\\.com|hotmail\\.com|outlook\\.com)$"
            if (!email.matches(Regex(emailPattern))) {
                binding.emailLayout.error = "Lütfen geçerli bir e-posta girin (@gmail.com, @hotmail.com vb.)"
                isValid = false
            }

            // Şifre Doğrulama (En az 6 karakter)
            if (password.length < 6) {
                binding.passwordLayout.error = "Şifre en az 6 karakter olmalıdır"
                isValid = false
            }

            // Eğer formatlar doğruysa Firebase ile kayıt işlemini başlatır
            if (isValid) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid

                            // Firestore'a kaydedilecek veri
                            val userMap = hashMapOf(
                                "username" to username,
                                "email" to email,
                                "score" to 0
                            )

                            userId?.let { uid ->
                                firestore.collection("Users").document(uid)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        // Başarılı kayıt sonrası Giriş ekranına döner
                                        findNavController().popBackStack()
                                    }
                                    .addOnFailureListener { e ->
                                        // Veritabanı bağlantı hataları için Toast kullanılabilir
                                        android.widget.Toast.makeText(requireContext(), "Veritabanı Hatası: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            // Firebase Kayıt Hataları
                            val exception = task.exception
                            when (exception) {
                                // E-posta zaten veritabanında kayıtlıysa
                                is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                                    binding.emailLayout.error = "Bu e-posta adresi zaten kullanımda"
                                }
                                else -> {
                                    binding.passwordLayout.error = "Kayıt başarısız oldu"
                                }
                            }
                        }
                    }
            }
        }

        // Zaten hesabım var yazısına tıklanınca Login ekranına döner
        binding.tvGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}