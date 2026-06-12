package com.cibertec.adoptapet.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.adoptapet.MainActivity
import com.cibertec.adoptapet.databinding.ActivityLoginBinding
import com.cibertec.adoptapet.utils.Validador

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            validarLogin()
        }

        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validarLogin() {
        val correo = binding.edtCorreoLogin.text.toString().trim()
        val password = binding.edtPasswordLogin.text.toString().trim()

        if (correo.isEmpty()) {
            binding.edtCorreoLogin.error = "Ingresa tu correo"
            return
        }

        if (!Validador.correoValido(correo)) {
            binding.edtCorreoLogin.error = "Correo no válido"
            return
        }

        if (password.isEmpty()) {
            binding.edtPasswordLogin.error = "Ingresa tu contraseña"
            return
        }

        if (!Validador.passwordValida(password)) {
            binding.edtPasswordLogin.error = "Mínimo 6 caracteres"
            return
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}