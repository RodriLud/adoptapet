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
import com.cibertec.adoptapet.data.FirebaseRepository
import com.cibertec.adoptapet.databinding.ActivityRegisterBinding
import com.cibertec.adoptapet.util.SystemBarUtils
import com.cibertec.adoptapet.util.Validador
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)

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
        val email = binding.edtCorreoRegistro.text.toString().trim()
        val fono = binding.edtTelefonoRegistro.text.toString().trim()
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

        if (!Validador.correoValido(email)) {
            binding.edtCorreoRegistro.error = "Correo no valido"
            return
        }

        if (!Validador.telefonoValido(fono)) {
            binding.edtTelefonoRegistro.error = "Telefono debe tener 9 digitos"
            return
        }

        if (!Validador.textoMinimo(direccion, 5)) {
            binding.edtDireccionRegistro.error = "Direccion demasiado corta"
            return
        }

        if (password.length < 6) {
            binding.edtPasswordRegistro.error = "Contrasena minima de 6 caracteres"
            return
        }

        cambiarEstadoCarga(true)
        repository.register(
            email = email,
            password = password,
            name = "$nombre $apellido",
            fono = fono,
            dni = dni,
            fechaNacimiento = fechaNacimiento,
            direccion = direccion,
            onSuccess = {
                cambiarEstadoCarga(false)
                Toast.makeText(this, getString(R.string.message_account_created), Toast.LENGTH_SHORT).show()
                goToMain()
            },
            onError = {
                cambiarEstadoCarga(false)
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun fechaValida(fecha: String): Boolean {
        return Regex("\\d{4}-\\d{2}-\\d{2}").matches(fecha)
    }

    private fun cambiarEstadoCarga(cargando: Boolean) {
        binding.btnRegistro.isEnabled = !cargando
        binding.btnRegistro.text = if (cargando) {
            "Registrando..."
        } else {
            getString(R.string.btn_register)
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
