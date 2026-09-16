package com.unscramble.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unscramble.app.R
import com.unscramble.app.adapter.LeaderboardAdapter
import com.unscramble.app.databinding.FragmentProfileBinding
import com.unscramble.app.model.User

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Firebase referanslarını başlatır
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView için LayoutManager ayarı
        binding.recyclerViewLeaderboard.layoutManager = LinearLayoutManager(requireContext())

        // Verileri çeker
        fetchUserData()
        fetchLeaderboard()

        // Çıkış Yap Butonu Tıklaması
        binding.btnLogout.setOnClickListener {
            auth.signOut() // Firebase oturumunu kapat

            // Geri tuşuyla tekrar profile dönmemek için:
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()

            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }
    }

    // Aktif kullanıcının bilgilerini çeker
    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {

            // İstek başlamadan önce ProgressBar'ı gösterir
            binding.progressBarProfile.visibility = View.VISIBLE

            firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener { document ->

                    // Veri geldiğinde ProgressBar'ı gizler, yazıları gösterir
                    binding.progressBarProfile.visibility = View.GONE
                    binding.tvProfileUsername.visibility = View.VISIBLE
                    binding.tvProfileScore.visibility = View.VISIBLE

                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        binding.tvProfileUsername.text = user?.username ?: "Bilinmeyen Kullanıcı"
                        binding.tvProfileScore.text = "En Yüksek Skor: ${user?.score ?: 0}"
                    }
                }
                .addOnFailureListener {
                    // Hata olursa animasyonu gizler
                    binding.progressBarProfile.visibility = View.GONE
                    Toast.makeText(requireContext(), "Kullanıcı bilgileri alınamadı.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Top 10 sıralamasını çeker
    private fun fetchLeaderboard() {
        firestore.collection("Users")
            .orderBy("score", Query.Direction.DESCENDING) // Puanları çoktan aza doğru sıralar
            .limit(10) // Sadece ilk 10 kişiyi alır
            .get()
            .addOnSuccessListener { result ->
                val leaderboardList = mutableListOf<User>()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    leaderboardList.add(user)
                }
                // Listeyi adaptöre gönderir ve RecyclerView'a bağlar
                binding.recyclerViewLeaderboard.adapter = LeaderboardAdapter(leaderboardList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Sıralama listesi alınamadı.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}