package com.cibertec.adoptapet.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.adoptapet.MainActivity
import com.cibertec.adoptapet.data.PetRepository
import com.cibertec.adoptapet.databinding.ActivityAdoptionSuccessBinding

class AdoptionSuccessActivity : AppCompatActivity() {
    private var _binding: ActivityAdoptionSuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAdoptionSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarMascota()
        configurarBotones()
    }

    private fun cargarMascota() {
        val petId = intent.getIntExtra("petId", -1)
        val pet = PetRepository.pets.find { it.id == petId }

        binding.txtMascotaSuccess.text =
            if (pet != null) {
                "Mascota: ${pet.name}"
            } else {
                "Mascota: No encontrada"
            }
    }

    private fun configurarBotones() {
        binding.btnVolverInicio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnVerSolicitudes.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("abrirSolicitudes", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}