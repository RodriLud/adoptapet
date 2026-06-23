package com.cibertec.adoptapet.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.cibertec.adoptapet.R
import com.cibertec.adoptapet.data.PetRepository
import com.cibertec.adoptapet.data.SessionManager
import com.cibertec.adoptapet.database.SolicitudLocalDao
import com.cibertec.adoptapet.databinding.ActivityAdoptionFormBinding
import com.cibertec.adoptapet.models.Pet
import com.cibertec.adoptapet.models.Solicitud
import com.cibertec.adoptapet.network.ApiClient
import com.cibertec.adoptapet.util.MultipartUtils
import com.cibertec.adoptapet.util.SystemBarUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdoptionFormActivity : AppCompatActivity() {
    private var _binding: ActivityAdoptionFormBinding? = null
    private val binding get() = _binding!!
    private var petId: Int = -1
    private var mascota: Pet? = null
    private var dniUri: Uri? = null
    private var domicilioUri: Uri? = null
    private lateinit var sessionManager: SessionManager
    private lateinit var solicitudLocalDao: SolicitudLocalDao

    private val seleccionarDniLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                dniUri = uri
                binding.btnArchivoDni.text = "DNI adjuntado"
            }
        }

    private val seleccionarDomicilioLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                domicilioUri = uri
                binding.btnArchivoDomicilio.text = "Comprobante adjuntado"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAdoptionFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollFormulario) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = barras.bottom + resources.getDimensionPixelSize(R.dimen.espacio_grande))
            insets
        }

        petId = intent.getIntExtra("petId", -1)
        sessionManager = SessionManager(this)
        solicitudLocalDao = SolicitudLocalDao(this)

        configurarToolbar()
        cargarMascota()
        configurarDropdowns()
        configurarArchivos()
        configurarBoton()
    }

    private fun configurarToolbar() {
        binding.toolbarFormulario.setNavigationOnClickListener {
            finish()
        }
    }

    private fun cargarMascota() {
        PetRepository.buscarMascota(this, petId) { pet ->
            if (pet == null) {
                Toast.makeText(this, "Mascota no encontrada", Toast.LENGTH_SHORT).show()
                finish()
                return@buscarMascota
            }

            mascota = pet
            binding.tvMascotaFormulario.text = "Adoptar a ${pet.name}"
        }
    }

    private fun configurarDropdowns() {
        configurarAdapter(binding.autoCompleteMotivo, arrayOf("Compania familiar", "Soporte emocional", "Seguridad", "Ayuda social", "Otro"))
        configurarAdapter(binding.autoCompleteVivienda, arrayOf("Casa", "Departamento", "Cuarto", "Condominio"))
        configurarAdapter(binding.autoCompleteExperiencia, arrayOf("Ninguna", "Basica", "Experimentado"))
        configurarAdapter(binding.autoCompleteOtrasMascotas, arrayOf("No", "Si"))
    }

    private fun configurarAdapter(view: android.widget.AutoCompleteTextView, opciones: Array<String>) {
        val adapter = ArrayAdapter(this, R.layout.item_dropdown_option, opciones)
        view.setAdapter(adapter)
        view.setDropDownBackgroundResource(android.R.color.white)
    }

    private fun configurarArchivos() {
        binding.btnArchivoDni.setOnClickListener {
            seleccionarDniLauncher.launch("image/*")
        }

        binding.btnArchivoDomicilio.setOnClickListener {
            seleccionarDomicilioLauncher.launch("image/*")
        }
    }

    private fun configurarBoton() {
        binding.btnEnviarSolicitud.setOnClickListener {
            if (validarFormulario()) {
                enviarSolicitud()
            }
        }
    }

    private fun enviarSolicitud() {
        val pet = mascota ?: return
        val idAdoptante = sessionManager.obtenerUserId()
        val personas = binding.edtCantidadPersonas.text.toString().trim().toInt()
        val comentarios = binding.edtMotivo.text.toString().trim()
        val motivo = binding.autoCompleteMotivo.text.toString().trim()
        val vivienda = binding.autoCompleteVivienda.text.toString().trim()
        val experiencia = binding.autoCompleteExperiencia.text.toString().trim()
        val otrasMascotas = binding.autoCompleteOtrasMascotas.text.toString().trim()

        solicitudLocalDao.guardarBorrador(
            idMascota = pet.id,
            motivo = motivo,
            vivienda = vivienda,
            experiencia = experiencia,
            otrasMascotas = otrasMascotas,
            personas = personas,
            comentarios = comentarios
        )

        cambiarEstadoCarga(true)

        val service = ApiClient.solicitudServicePublica()

        val idAdoptantePart = okhttp3.MultipartBody.Part.createFormData("id_adoptante", idAdoptante.toString())
        val idMascotaPart = okhttp3.MultipartBody.Part.createFormData("id_mascota", pet.id.toString())
        val comentarioPart = okhttp3.MultipartBody.Part.createFormData("comentario", comentarios)
        val motivoPart = okhttp3.MultipartBody.Part.createFormData("motivo_adopcion", motivo)
        val viviendaPart = okhttp3.MultipartBody.Part.createFormData("tipo_vivienda", vivienda)
        val experienciaPart = okhttp3.MultipartBody.Part.createFormData("experiencia_mascotas", experiencia)
        val otrasMascotasPart = okhttp3.MultipartBody.Part.createFormData("otras_mascotas", otrasMascotas)
        val cantidadPersonasPart = okhttp3.MultipartBody.Part.createFormData("cantidad_personas_hogar", personas.toString())
        val comentariosAdicionalesPart = okhttp3.MultipartBody.Part.createFormData("comentarios_adicionales", comentarios)

        val archivoDniPart = MultipartUtils.archivo(this, "archivo_dni", dniUri)
        val archivoDomicilioPart = MultipartUtils.archivo(this, "archivo_domicilio", domicilioUri)

        // Enviamos el formulario limpio
        service.registrarSolicitud(
            idAdoptante = idAdoptantePart,
            idMascota = idMascotaPart,
            comentario = comentarioPart,
            motivoAdopcion = motivoPart,
            tipoVivienda = viviendaPart,
            experienciaMascotas = experienciaPart,
            otrasMascotas = otrasMascotasPart,
            cantidadPersonasHogar = cantidadPersonasPart,
            comentariosAdicionales = comentariosAdicionalesPart,
            archivoDni = archivoDniPart,
            archivoDomicilio = archivoDomicilioPart
        ).enqueue(object : Callback<Solicitud> {
            override fun onResponse(call: Call<Solicitud>, response: Response<Solicitud>) {
                cambiarEstadoCarga(false)

                val solicitud = response.body()
                if (!response.isSuccessful || solicitud == null) {
                    Toast.makeText(this@AdoptionFormActivity, "No se pudo enviar la solicitud", Toast.LENGTH_LONG).show()
                    return
                }

                solicitudLocalDao.guardarSolicitudPendiente(solicitud, pet.id, pet.name)

                val intent = Intent(this@AdoptionFormActivity, AdoptionSuccessActivity::class.java)
                intent.putExtra("petId", pet.id)
                startActivity(intent)
                finish()
            }

            override fun onFailure(call: Call<Solicitud>, t: Throwable) {
                cambiarEstadoCarga(false)
                Toast.makeText(
                    this@AdoptionFormActivity,
                    "Solicitud guardada como borrador. Reintenta cuando haya conexion.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun validarFormulario(): Boolean {
        val idAdoptante = sessionManager.obtenerUserId()
        val motivo = binding.autoCompleteMotivo.text.toString().trim()
        val vivienda = binding.autoCompleteVivienda.text.toString().trim()
        val experiencia = binding.autoCompleteExperiencia.text.toString().trim()
        val otrasMascotas = binding.autoCompleteOtrasMascotas.text.toString().trim()
        val cantidadPersonas = binding.edtCantidadPersonas.text.toString().trim()
        val comentarios = binding.edtMotivo.text.toString().trim()

        if (idAdoptante <= 0) {
            Toast.makeText(this, "Inicia sesion nuevamente", Toast.LENGTH_SHORT).show()
            return false
        }

        if (motivo.isEmpty()) {
            Toast.makeText(this, "Selecciona el motivo de adopcion", Toast.LENGTH_SHORT).show()
            return false
        }

        if (vivienda.isEmpty()) {
            Toast.makeText(this, "Selecciona el tipo de vivienda", Toast.LENGTH_SHORT).show()
            return false
        }

        if (experiencia.isEmpty()) {
            Toast.makeText(this, "Selecciona tu experiencia con mascotas", Toast.LENGTH_SHORT).show()
            return false
        }

        if (otrasMascotas.isEmpty()) {
            Toast.makeText(this, "Indica si tienes otras mascotas", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cantidadPersonas.isEmpty() || cantidadPersonas.toIntOrNull() == null) {
            binding.edtCantidadPersonas.error = "Ingresa una cantidad valida"
            return false
        }

        if (comentarios.length < 10) {
            binding.edtMotivo.error = "Agrega un comentario de al menos 10 caracteres"
            return false
        }

        if (dniUri == null) {
            Toast.makeText(this, "Adjunta una foto de tu DNI", Toast.LENGTH_SHORT).show()
            return false
        }

        if (domicilioUri == null) {
            Toast.makeText(this, "Adjunta el comprobante de domicilio", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun cambiarEstadoCarga(cargando: Boolean) {
        binding.btnEnviarSolicitud.isEnabled = !cargando
        binding.btnEnviarSolicitud.text = if (cargando) {
            "Enviando..."
        } else {
            getString(R.string.adoption_form_send)
        }
    }
}
