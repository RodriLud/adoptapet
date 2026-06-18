package com.cibertec.adoptapet.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.adoptapet.adapters.PetAdapter
import com.cibertec.adoptapet.data.FavoritesRepository
import com.cibertec.adoptapet.databinding.FragmentFavoritesBinding
import com.cibertec.adoptapet.views.PetDetailActivity

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var petAdapter: PetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        petAdapter = PetAdapter(FavoritesRepository.obtenerFavoritos()) { pet ->
            val intent = Intent(requireContext(), PetDetailActivity::class.java)
            intent.putExtra("petId", pet.id)
            startActivity(intent)
        }

        binding.rvFavoritos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavoritos.adapter = petAdapter

        actualizarVista()
    }

    override fun onResume() {
        super.onResume()
        if (::petAdapter.isInitialized) {
            actualizarVista()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun actualizarVista() {
        val favoritos = FavoritesRepository.obtenerFavoritos()

        petAdapter.actualizarLista(favoritos)

        if (favoritos.isEmpty()) {
            binding.txtFavoritosVacio.visibility = View.VISIBLE
            binding.rvFavoritos.visibility = View.GONE
        } else {
            binding.txtFavoritosVacio.visibility = View.GONE
            binding.rvFavoritos.visibility = View.VISIBLE
        }
    }
}