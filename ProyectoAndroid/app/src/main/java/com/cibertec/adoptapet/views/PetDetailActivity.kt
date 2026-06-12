package com.cibertec.adoptapet.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.adoptapet.data.PetRepository
import com.cibertec.adoptapet.databinding.ActivityPetDetailBinding

class PetDetailActivity : AppCompatActivity() {

    private var _binding: ActivityPetDetailBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityPetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        cargarDatosMascota()
    }

    private fun configurarToolbar() {
        binding.toolbarDetalle.setNavigationOnClickListener {
            finish()
        }
    }

    private fun cargarDatosMascota() {
        val petId = intent.getIntExtra("petId", -1)

        val pet = PetRepository.pets.find { it.id == petId }

        if (pet == null) {
            Toast.makeText(this, "Mascota no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.imgDetalleMascota.setImageResource(pet.imageRes)
        binding.txtDetalleNombre.text = pet.name
        binding.txtDetalleInfo.text = "${pet.type} • ${pet.age} • ${pet.gender}"
        binding.txtDetalleSize.text = pet.size
        binding.txtDetalleEsterilizado.text =
            if (pet.sterilized) "Esterilizado: Sí" else "Esterilizado: No"
        binding.txtDetalleDescripcion.text = pet.description

        binding.btnSolicitarAdopcion.setOnClickListener {
            val intent = Intent(this, AdoptionFormActivity::class.java)
            intent.putExtra("petId", pet.id)
            startActivity(intent)
        }
    }
}