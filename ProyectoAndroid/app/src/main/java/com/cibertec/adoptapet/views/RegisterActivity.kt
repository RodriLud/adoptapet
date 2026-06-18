package com.cibertec.adoptapet.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.adoptapet.MainActivity
import com.cibertec.adoptapet.databinding.ActivityRegisterBinding
import com.cibertec.adoptapet.utils.Validador
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistro.setOnClickListener {
            validarRegistro()
        }

        binding.tvIrLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validarRegistro() {
        val nombre = binding.edtNombreRegistro.text.toString().trim()
        val correo = binding.edtCorreoRegistro.text.toString().trim()
        val telefono = binding.edtTelefonoRegistro.text.toString().trim()
        val password = binding.edtPasswordRegistro.text.toString().trim()

        if (!Validador.textoMinimo(nombre, 3)) {
            binding.edtNombreRegistro.error = "Nombre mínimo 3 caracteres"
            return
        }

        if (!Validador.correoValido(correo)) {
            binding.edtCorreoRegistro.error = "Correo no válido"
            return
        }

        if (!Validador.telefonoValido(telefono)) {
            binding.edtTelefonoRegistro.error = "Teléfono debe tener 9 dígitos"
            return
        }

        if (!Validador.passwordValida(password)) {
            binding.edtPasswordRegistro.error = "Contraseña mínima de 6 caracteres"
            return
        }

        val usuario = hashMapOf(
            "nombre" to nombre,
            "correo" to correo,
            "telefono" to telefono,
            "password" to password
        )

        db.collection("usuarios")
            .add(usuario)
            .addOnSuccessListener {
                Toast.makeText(this, "Registrado en Firebase", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}