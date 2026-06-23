package com.cibertec.adoptapet.views

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.adoptapet.R
import com.cibertec.adoptapet.data.RequestRepository
import com.cibertec.adoptapet.data.SessionManager
import com.cibertec.adoptapet.databinding.ActivityRequestDetailBinding
import com.cibertec.adoptapet.models.AdoptionRequest
import com.cibertec.adoptapet.util.SystemBarUtils

class RequestDetailActivity : AppCompatActivity() {

    private var _binding: ActivityRequestDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var requestId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)

        sessionManager = SessionManager(this)
        requestId = intent.getIntExtra("requestId", -1)

        binding.toolbarDetalleSolicitud.setNavigationOnClickListener {
            finish()
        }

        cargarSolicitud()
    }

    private fun cargarSolicitud() {
        RequestRepository.buscarSolicitud(this, sessionManager, requestId) { solicitud ->
            if (solicitud == null) {
                Toast.makeText(this, "Solicitud no encontrada", Toast.LENGTH_SHORT).show()
                finish()
                return@buscarSolicitud
            }

            pintarSolicitud(solicitud)
        }
    }

    private fun pintarSolicitud(solicitud: AdoptionRequest) {
        binding.txtMascotaSolicitud.text = solicitud.petName
        binding.txtCodigoSolicitud.text = "Solicitud ADP-${solicitud.id} - ${solicitud.date}"
        binding.txtEstadoSolicitud.text = solicitud.status.replace("_", " ")
        binding.txtEstadoSolicitud.setTextColor(colorEstado(solicitud.status))
        binding.txtMensajeEstado.text = mensajePorEstado(solicitud)

        val tieneProgramacion = !solicitud.deliveryDate.isNullOrBlank()
        binding.cardProgramacion.visibility = if (tieneProgramacion) View.VISIBLE else View.GONE

        if (tieneProgramacion) {
            binding.txtFechaEntrega.text = "Fecha: ${solicitud.deliveryDate}"
            binding.txtHoraEntrega.text = "Horario: ${solicitud.deliveryStart ?: "-"} - ${solicitud.deliveryEnd ?: "-"}"
            binding.txtLimiteRecojo.text = "Limite de recojo: ${solicitud.pickupLimit ?: "Segun indicacion del refugio"}"
        }
    }

    private fun mensajePorEstado(solicitud: AdoptionRequest): String {
        return when (solicitud.status.uppercase()) {
            "PENDIENTE" -> "Tu solicitud fue enviada y esta siendo revisada por el equipo."
            "APROBADA" -> "Tu solicitud fue aprobada. Revisa la fecha y hora de entrega programada."
            "RECHAZADA" -> "Tu solicitud fue rechazada. Motivo: ${solicitud.rejectionReason ?: "No indicado"}"
            "FINALIZADA" -> "La adopcion fue finalizada. Gracias por darle un hogar a ${solicitud.petName}."
            "CANCELADA" -> "La solicitud fue cancelada. Puedes revisar otras mascotas disponibles."
            "NO_ASISTIO" -> "Se registro inasistencia a la entrega. El refugio podra reprogramar o cancelar el proceso."
            else -> "Estado actualizado por el refugio."
        }
    }

    private fun colorEstado(estado: String): Int {
        return when (estado.uppercase()) {
            "APROBADA", "FINALIZADA" -> getColor(R.color.color_verde)
            "RECHAZADA", "CANCELADA", "NO_ASISTIO" -> getColor(R.color.color_rojo)
            else -> getColor(R.color.color_naranja)
        }
    }
}
