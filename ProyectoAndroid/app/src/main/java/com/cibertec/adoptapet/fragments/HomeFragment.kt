package com.cibertec.adoptapet.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.adoptapet.adapters.PetAdapter
import com.cibertec.adoptapet.data.PetRepository
import com.cibertec.adoptapet.databinding.FragmentHomeBinding
import com.cibertec.adoptapet.views.PetDetailActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var petAdapter: PetAdapter
    private var categoriaActual = "Todas"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarRecyclerView()
        configurarBotones()
        configurarBuscador()
    }

    private fun configurarRecyclerView() {
        petAdapter = PetAdapter(PetRepository.pets) { pet ->
            val intent = Intent(requireContext(), PetDetailActivity::class.java)
            intent.putExtra("petId", pet.id)
            startActivity(intent)
        }

        binding.rvMascotas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMascotas.adapter = petAdapter
    }

    private fun configurarBotones() {
        binding.btnTodas.setOnClickListener {
            categoriaActual = "Todas"
            filtrarPets()
        }

        binding.btnPerros.setOnClickListener {
            categoriaActual = "Perro"
            filtrarPets()
        }

        binding.btnGatos.setOnClickListener {
            categoriaActual = "Gato"
            filtrarPets()
        }
    }

    private fun configurarBuscador() {
        binding.edtBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filtrarPets()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}
        })
    }

    private fun filtrarPets() {
        val texto = binding.edtBuscar.text.toString().trim().lowercase()

        val listaFiltrada = PetRepository.pets.filter { pet ->
            val coincideCategoria =
                categoriaActual == "Todas" || pet.type == categoriaActual

            val coincideBusqueda =
                pet.name.lowercase().contains(texto)

            coincideCategoria && coincideBusqueda
        }

        petAdapter.actualizarLista(listaFiltrada)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}