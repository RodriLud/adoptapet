package com.cibertec.adoptapet.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.adoptapet.adapters.PetAdapter
import com.cibertec.adoptapet.data.PetRepository
import com.cibertec.adoptapet.databinding.FragmentHomeBinding
import com.cibertec.adoptapet.models.Pet
import com.cibertec.adoptapet.views.PetDetailActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var petAdapter: PetAdapter
    private var categoriaActual = "Todas"
    private var mascotasActuales: List<Pet> = emptyList()

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
        cargarMascotas()
    }

    private fun configurarRecyclerView() {
        petAdapter = PetAdapter(
            pets = emptyList(),
            onItemClick = { pet ->
                val intent = Intent(requireContext(), PetDetailActivity::class.java)
                intent.putExtra("petId", pet.id)
                startActivity(intent)
            }
        )

        binding.rvMascotas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMascotas.adapter = petAdapter
    }

    private fun cargarMascotas() {
        PetRepository.cargarMascotas(requireContext()) { mascotas, desdeServidor ->
            if (_binding == null) return@cargarMascotas
            mascotasActuales = mascotas
            filtrarPets()

            if (mascotas.isEmpty()) {
                Toast.makeText(requireContext(), "No hay mascotas disponibles", Toast.LENGTH_SHORT).show()
            } else if (!desdeServidor) {
                Toast.makeText(requireContext(), "Mostrando mascotas guardadas en el celular", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarBotones() {
        binding.btnTodas.setOnClickListener {
            categoriaActual = "Todas"
            actualizarFiltroSeleccionado()
            filtrarPets()
        }

        binding.btnPerros.setOnClickListener {
            categoriaActual = "Perro"
            actualizarFiltroSeleccionado()
            filtrarPets()
        }

        binding.btnGatos.setOnClickListener {
            categoriaActual = "Gato"
            actualizarFiltroSeleccionado()
            filtrarPets()
        }

        actualizarFiltroSeleccionado()
    }

    private fun actualizarFiltroSeleccionado() {
        binding.btnTodas.isChecked = categoriaActual == "Todas"
        binding.btnPerros.isChecked = categoriaActual == "Perro"
        binding.btnGatos.isChecked = categoriaActual == "Gato"
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

        val listaFiltrada = mascotasActuales.filter { pet ->
            val coincideCategoria =
                categoriaActual == "Todas" || pet.type.equals(categoriaActual, ignoreCase = true)

            val coincideBusqueda =
                pet.name.lowercase().contains(texto) ||
                    pet.breed.lowercase().contains(texto)

            coincideCategoria && coincideBusqueda
        }

        petAdapter.actualizarLista(listaFiltrada)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
