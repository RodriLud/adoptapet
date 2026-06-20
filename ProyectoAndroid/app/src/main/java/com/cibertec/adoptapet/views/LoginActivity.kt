package com.cibertec.adoptapet.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.cibertec.adoptapet.MainActivity
import com.cibertec.adoptapet.R
import com.cibertec.adoptapet.data.FirebaseRepository
import com.cibertec.adoptapet.databinding.ActivityLoginBinding
import com.cibertec.adoptapet.util.SystemBarUtils

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)

        if (repository.isUserLoggedIn()) {
            goToMain()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollLogin) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = barras.bottom + resources.getDimensionPixelSize(R.dimen.espacio_grande))
            insets
        }

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }


    private fun validate(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            showMessage(getString(R.string.error_login_required))
            return false
        }

        if (password.length < 6) {
            showMessage(getString(R.string.error_password_length))
            return false
        }
        return true
    }

    private fun login() {
        val email = binding.editEmailLogin.text.toString().trim()
        val password = binding.editPasswordLogin.text.toString().trim()

        if (!validate(email, password)) return

        cambiarEstadoCarga(true)

        repository.login(
            email = email,
            password = password,
            onSuccess = {
                val uid = repository.getCurrentUserUid()
                if (uid != null) {
                    repository.getUserData(uid,
                        onSuccess = { data ->
                            cambiarEstadoCarga(false)
                            val rol = data?.get("rol") as? String ?: "ROLE_ADOPTANTE"
                            
                            // Aquí puedes redirigir según el rol
                            if (rol == "ROLE_ADMIN") {
                                // Por ahora lo mandamos a Main, pero podrías tener AdminActivity
                                goToMain()
                            } else {
                                goToMain()
                            }
                        },
                        onError = { error ->
                            cambiarEstadoCarga(false)
                            showMessage("Error al obtener perfil: $error")
                        }
                    )
                } else {
                    cambiarEstadoCarga(false)
                    goToMain()
                }
            },
            onError = { error ->
                cambiarEstadoCarga(false)
                showMessage(error)
            }
        )
    }

    private fun cambiarEstadoCarga(cargando: Boolean) {
        binding.btnLogin.isEnabled = !cargando
        binding.btnLogin.text = if (cargando) {
            "Ingresando..."
        } else {
            getString(R.string.btn_login)
        }
    }


    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
