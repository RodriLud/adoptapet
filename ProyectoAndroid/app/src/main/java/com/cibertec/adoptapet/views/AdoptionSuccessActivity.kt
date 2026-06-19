package com.cibertec.adoptapet.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.cibertec.adoptapet.MainActivity
import com.cibertec.adoptapet.R
import com.cibertec.adoptapet.data.PetRepository
import com.cibertec.adoptapet.databinding.ActivityAdoptionSuccessBinding
import com.cibertec.adoptapet.util.SystemBarUtils

class AdoptionSuccessActivity : AppCompatActivity() {
    private var _binding: ActivityAdoptionSuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAdoptionSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.contenedorSuccess) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = barras.bottom + resources.getDimensionPixelSize(R.dimen.espacio_grande))
            insets
        }

        cargarMascota()
        configurarBotones()
    }

    private fun cargarMascota() {
        val petId = intent.getIntExtra("petId", -1)
        PetRepository.buscarMascota(this, petId) { pet ->
            binding.txtMascotaSuccess.text =
                if (pet != null) {
                    "Mascota: ${pet.name}"
                } else {
                    "Mascota: solicitud registrada"
                }
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
