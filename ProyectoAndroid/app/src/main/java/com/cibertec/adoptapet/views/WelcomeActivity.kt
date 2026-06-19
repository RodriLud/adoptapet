package com.cibertec.adoptapet.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.cibertec.adoptapet.MainActivity
import com.cibertec.adoptapet.data.SessionManager
import com.cibertec.adoptapet.databinding.ActivityWelcomeBinding
import com.cibertec.adoptapet.util.SystemBarUtils

class WelcomeActivity : AppCompatActivity() {

    private var _binding: ActivityWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.contenedorWelcome) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = barras.bottom + resources.getDimensionPixelSize(com.cibertec.adoptapet.R.dimen.espacio_grande))
            insets
        }

        if (SessionManager(this).haySesionActiva()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnIrLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }
}
