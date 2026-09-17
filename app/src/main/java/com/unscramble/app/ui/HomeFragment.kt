package com.unscramble.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.unscramble.app.R
import com.unscramble.app.adapter.GameAdapter
import com.unscramble.app.databinding.FragmentHomeBinding
import com.unscramble.app.model.Game

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Şimdilik listeyi manuel dolduruyoruz
        val gameList = listOf(
            Game(1, "Unscramble", "Karışık harflerden doğru kelimeyi bul, puanları topla!"),
            Game(2, "Bayrak Bulmaca", "Ülke bayraklarını tahmin et, coğrafya bilgini sına!")
        )

        val adapter = GameAdapter(gameList) { selectedGame ->
            when (selectedGame.id) {
                1 -> findNavController().navigate(R.id.action_homeFragment_to_UnscrambleGameFragment)
                2 -> findNavController().navigate(R.id.action_homeFragment_to_flagGameFragment)
            }
        }

        // RecyclerView ayarları
        binding.recyclerViewGames.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewGames.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}