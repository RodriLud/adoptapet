package com.cibertec.adoptapet.views

import android.app.DatePickerDialog
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
import com.cibertec.adoptapet.databinding.ActivityRegisterBinding
import com.cibertec.adoptapet.models.Adoptante
import com.cibertec.adoptapet.models.Usuario
import com.cibertec.adoptapet.network.ApiClient
import com.cibertec.adoptapet.util.SystemBarUtils
import com.cibertec.adoptapet.util.Validador
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)
        sessionManager = SessionManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRegistro) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = barras.bottom + resources.getDimensionPixelSize(R.dimen.espacio_grande))
            insets
        }

        configurarSelectorFecha()

        binding.btnRegistro.setOnClickListener {
            validarRegistro()
        }

        binding.tvIrLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun configurarSelectorFecha() {
        val abrirSelector = {
            val calendario = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val mes = (month + 1).toString().padStart(2, '0')
                    val dia = day.toString().padStart(2, '0')
                    binding.edtFechaNacimientoRegistro.setText("$year-$mes-$dia")
                },
                calendario.get(Calendar.YEAR) - 18,
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.edtFechaNacimientoRegistro.setOnClickListener { abrirSelector() }
        binding.layoutFechaNacimientoRegistro.setEndIconOnClickListener { abrirSelector() }
    }

    private fun validarRegistro() {
        val nombre = binding.edtNombreRegistro.text.toString().trim()
        val apellido = binding.edtApellidoRegistro.text.toString().trim()
        val dni = binding.edtDniRegistro.text.toString().trim()
        val fechaNacimiento = binding.edtFechaNacimientoRegistro.text.toString().trim()
        val correo = binding.edtCorreoRegistro.text.toString().trim()
        val telefono = binding.edtTelefonoRegistro.text.toString().trim()
        val direccion = binding.edtDireccionRegistro.text.toString().trim()
        val password = binding.edtPasswordRegistro.text.toString().trim()

        if (!Validador.textoMinimo(nombre, 3)) {
            binding.edtNombreRegistro.error = "Nombre minimo 3 caracteres"
            return
        }

        if (!Validador.textoMinimo(apellido, 3)) {
            binding.edtApellidoRegistro.error = "Apellido minimo 3 caracteres"
            return
        }

        if (!Validador.dniValido(dni)) {
            binding.edtDniRegistro.error = "DNI debe tener 8 digitos"
            return
        }

        if (!fechaValida(fechaNacimiento)) {
            binding.edtFechaNacimientoRegistro.error = "Usa el formato yyyy-MM-dd"
            return
        }

        if (!Validador.correoValido(correo)) {
            binding.edtCorreoRegistro.error = "Correo no valido"
            return
        }

        if (correo.length > 50) {
            binding.edtCorreoRegistro.error = "Correo maximo 50 caracteres"
            return
        }

        if (!Validador.telefonoValido(telefono)) {
            binding.edtTelefonoRegistro.error = "Telefono debe tener 9 digitos"
            return
        }

        if (!Validador.textoMinimo(direccion, 5)) {
            binding.edtDireccionRegistro.error = "Direccion demasiado corta"
            return
        }

        if (!Validador.passwordValida(password)) {
            binding.edtPasswordRegistro.error = "Contrasena minima de 6 caracteres"
            return
        }

        registrarAdoptante(
            Adoptante(
                username = correo,
                password = password,
                nom_adoptante = nombre,
                ape_adoptante = apellido,
                dni = dni,
                fec_nacimiento = fechaNacimiento,
                email = correo,
                telefono = telefono,
                direccion = direccion
            ),
            password
        )
    }

    private fun registrarAdoptante(adoptante: Adoptante, password: String) {
        cambiarEstadoCarga(true)

        ApiClient.authService().register(adoptante).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                cambiarEstadoCarga(false)

                val user = response.body()
                if (!response.isSuccessful || user == null) {
                    Toast.makeText(
                        this@RegisterActivity,
                        obtenerMensajeError(response),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                sessionManager.guardarSesion(user, password)
                Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                cambiarEstadoCarga(false)
                Toast.makeText(
                    this@RegisterActivity,
                    "No se pudo conectar: ${t.message ?: "servidor no disponible"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun fechaValida(fecha: String): Boolean {
        return Regex("\\d{4}-\\d{2}-\\d{2}").matches(fecha)
    }

    private fun obtenerMensajeError(response: Response<Usuario>): String {
        val fallback = "No se pudo registrar. Revisa los datos ingresados."
        val errorJson = response.errorBody()?.string().orEmpty()

        if (errorJson.isBlank()) {
            return fallback
        }

        return try {
            Gson().fromJson(errorJson, ApiError::class.java)?.mensaje?.takeIf { it.isNotBlank() }
                ?: fallback
        } catch (e: JsonSyntaxException) {
            fallback
        }
    }

    private fun cambiarEstadoCarga(cargando: Boolean) {
        binding.btnRegistro.isEnabled = !cargando
        binding.btnRegistro.text = if (cargando) {
            "Registrando..."
        } else {
            getString(R.string.btn_register)
        }
    }

    private data class ApiError(val mensaje: String?)
}
