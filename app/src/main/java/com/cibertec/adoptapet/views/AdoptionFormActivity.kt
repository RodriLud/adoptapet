package com.cibertec.adoptapet.views

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.adoptapet.data.PetRepository
import com.cibertec.adoptapet.databinding.ActivityAdoptionFormBinding
import com.cibertec.adoptapet.models.AdoptionRequest
import com.cibertec.adoptapet.utils.Validador
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdoptionFormActivity : AppCompatActivity() {
    private var _binding: ActivityAdoptionFormBinding? = null
    private val binding get() = _binding!!
    private var petId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAdoptionFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        petId = intent.getIntExtra("petId", -1)

        configurarToolbar()
        cargarMascota()
        configurarDropdown()
        configurarBoton()
    }

    private fun configurarToolbar() {
        binding.toolbarFormulario.setNavigationOnClickListener {
            finish()
        }
    }

    private fun cargarMascota() {
        val pet = PetRepository.pets.find { it.id == petId }

        if (pet == null) {
            Toast.makeText(this, "Mascota no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvMascotaFormulario.text = "Adoptar a ${pet.name}"
    }

    private fun configurarDropdown() {
        val opciones = arrayOf("Si", "No")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
        binding.autoCompleteExperiencia.setAdapter(adapter)
    }

    private fun configurarBoton() {
        binding.btnEnviarSolicitud.setOnClickListener {
            if (validarFormulario()) {
                guardarSolicitud()

                val intent = Intent(this, AdoptionSuccessActivity::class.java)
                intent.putExtra("petId", petId)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun guardarSolicitud() {
        val pet = PetRepository.pets.find { it.id == petId } ?: return

        val fechaActual = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(Date())

        val nuevaSolicitud = AdoptionRequest(
            id = 1,
            petId = pet.id,
            petName = pet.name,
            applicantName = binding.edtNombre.text.toString().trim(),
            dni = binding.edtDni.text.toString().trim(),
            phone = binding.edtTelefono.text.toString().trim(),
            email = binding.edtCorreo.text.toString().trim(),
            address = binding.edtDireccion.text.toString().trim(),
            hasExperience = binding.autoCompleteExperiencia.text.toString(),
            reason = binding.edtMotivo.text.toString().trim(),
            date = fechaActual,
            status = "Pendiente"
        )

        // TODO: Repositorio solicitud.guardar(nuevaSolicitud)
    }

    private fun validarFormulario(): Boolean {
        val nombre = binding.edtNombre.text.toString().trim()
        val dni = binding.edtDni.text.toString().trim()
        val telefono = binding.edtTelefono.text.toString().trim()
        val correo = binding.edtCorreo.text.toString().trim()
        val direccion = binding.edtDireccion.text.toString().trim()
        val experiencia = binding.autoCompleteExperiencia.text.toString()
        val motivo = binding.edtMotivo.text.toString().trim()

        if (!Validador.textoMinimo(nombre, 3)) {
            binding.edtNombre.error = "Nombre mínimo 3 caracteres"
            return false
        }

        if (!Validador.dniValido(dni)) {
            binding.edtDni.error = "DNI debe tener 8 dígitos"
            return false
        }

        if (!Validador.telefonoValido(telefono)) {
            binding.edtTelefono.error = "Teléfono debe tener 9 dígitos"
            return false
        }

        if (!Validador.correoValido(correo)) {
            binding.edtCorreo.error = "Correo no válido"
            return false
        }

        if (!Validador.textoMinimo(direccion, 5)) {
            binding.edtDireccion.error = "Dirección demasiado corta"
            return false
        }

        if (experiencia.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione si tiene experiencia", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Validador.textoMinimo(motivo, 10)) {
            binding.edtMotivo.error = "Explica mejor el motivo"
            return false
        }

        return true
    }
}