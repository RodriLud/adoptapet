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
import com.cibertec.adoptapet.data.SessionManager
import com.cibertec.adoptapet.databinding.ActivityLoginBinding
import com.cibertec.adoptapet.models.Login
import com.cibertec.adoptapet.models.Usuario
import com.cibertec.adoptapet.network.ApiClient
import com.cibertec.adoptapet.util.SystemBarUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)
        sessionManager = SessionManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollLogin) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = barras.bottom + resources.getDimensionPixelSize(R.dimen.espacio_grande))
            insets
        }

        if (sessionManager.haySesionActiva()) {
            abrirAppPrincipal()
            return
        }

        binding.btnLogin.setOnClickListener {
            validarLogin()
        }

        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validarLogin() {
        val usuario = binding.edtCorreoLogin.text.toString().trim()
        val password = binding.edtPasswordLogin.text.toString().trim()

        if (usuario.isEmpty()) {
            binding.edtCorreoLogin.error = "Ingresa tu usuario o correo"
            return
        }

        if (password.isEmpty()) {
            binding.edtPasswordLogin.error = "Ingresa tu contrasena"
            return
        }

        iniciarSesion(usuario, password)
    }

    private fun iniciarSesion(usuario: String, password: String) {
        cambiarEstadoCarga(true)

        val login = Login(username = usuario, password = password)
        ApiClient.authService().login(login).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                cambiarEstadoCarga(false)

                val user = response.body()
                if (!response.isSuccessful || user == null) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Usuario o contrasena incorrectos",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                if (user.rol != "ROLE_ADOPTANTE") {
                    Toast.makeText(
                        this@LoginActivity,
                        "Esta app es solo para adoptantes",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                sessionManager.guardarSesion(user, password)
                abrirAppPrincipal()
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                cambiarEstadoCarga(false)
                Toast.makeText(
                    this@LoginActivity,
                    "No se pudo conectar: ${t.message ?: "servidor no disponible"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun cambiarEstadoCarga(cargando: Boolean) {
        binding.btnLogin.isEnabled = !cargando
        binding.btnLogin.text = if (cargando) {
            "Ingresando..."
        } else {
            getString(R.string.btn_login)
        }
    }

    private fun abrirAppPrincipal() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
