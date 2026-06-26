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
import com.cibertec.adoptapet.data.SessionManager
import com.cibertec.adoptapet.databinding.ActivityLoginBinding
import com.cibertec.adoptapet.models.Usuario
import com.cibertec.adoptapet.util.SystemBarUtils

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)

        sessionManager = SessionManager(this)

        if (repository.isUserLoggedIn()) {
            if (sessionManager.haySesionActiva()) {
                goToMain()
                return
            } else {
                recuperarSesionAutomatica()
                return
            }
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

                            if (data != null) {
                                val idUsuario = (data["id_usuario"] as? Long)?.toInt()
                                    ?: (data["id_usuario"] as? String)?.toIntOrNull()
                                    ?: -1

                                val username =
                                    data["username"] as? String ?: email.substringBefore("@")
                                val rol = data["rol"] as? String ?: "ROLE_ADOPTANTE"

                                val usuarioLogueado = Usuario(
                                    id_usuario = idUsuario,
                                    username = username,
                                    rol = rol,
                                    activo = data["activo"] as? Boolean ?: true,
                                    nom_adoptante = data["nom_adoptante"] as? String ?: "",
                                    ape_adoptante = data["ape_adoptante"] as? String ?: "",
                                    dni = data["dni"] as? String ?: "",
                                    fec_nacimiento = data["fec_nacimiento"] as? String ?: "",
                                    email = data["email"] as? String ?: email,
                                    telefono = data["telefono"] as? String ?: "",
                                    direccion = data["direccion"] as? String ?: ""
                                )
                                sessionManager.guardarSesion(usuarioLogueado, password)
                            }

                            goToMain()
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

    private fun recuperarSesionAutomatica() {
        val uid = repository.getCurrentUserUid()
        if (uid != null) {
            cambiarEstadoCarga(true)
            repository.getUserData(uid,
                onSuccess = { data ->
                    cambiarEstadoCarga(false)
                    if (data != null) {
                        val idUsuario = (data["id_usuario"] as? Long)?.toInt()
                            ?: (data["id_usuario"] as? String)?.toIntOrNull()
                            ?: -1

                        val username = data["username"] as? String ?: "usuario"
                        val rol = data["rol"] as? String ?: "ROLE_ADOPTANTE"

                        val usuarioLogueado = Usuario(
                            id_usuario = idUsuario,
                            username = username,
                            rol = rol,
                            activo = data["activo"] as? Boolean ?: true,
                            nom_adoptante = data["nom_adoptante"] as? String ?: "",
                            ape_adoptante = data["ape_adoptante"] as? String ?: "",
                            dni = data["dni"] as? String ?: "",
                            fec_nacimiento = data["fec_nacimiento"] as? String ?: "",
                            email = data["email"] as? String ?: "",
                            telefono = data["telefono"] as? String ?: "",
                            direccion = data["direccion"] as? String ?: ""
                        )
                        sessionManager.guardarSesion(usuarioLogueado, "")
                    }
                    goToMain()
                },
                onError = { error ->
                    cambiarEstadoCarga(false)
                    showMessage("Error al sincronizar sesión: $error")
                }
            )
        } else {
            goToMain()
        }
    }

    private fun cambiarEstadoCarga(cargando: Boolean) {
        binding.btnLogin.isEnabled = !cargando
        binding.btnLogin.text = if (cargando) "Ingresando..." else getString(R.string.btn_login)
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