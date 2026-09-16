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

            // İleride başka oyunlar eklenecek
        )

        // Adapter'ı başlatıp tıklama olayını tanımlar
        val adapter = GameAdapter(gameList) { selectedGame ->
            if (selectedGame.id == 1) {
                // Unscramble seçildiyse oyun ekranına yönlendirir
                findNavController().navigate(R.id.action_homeFragment_to_gameFragment)
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