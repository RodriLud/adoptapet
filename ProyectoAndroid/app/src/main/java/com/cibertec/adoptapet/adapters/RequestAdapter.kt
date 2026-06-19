package com.cibertec.adoptapet.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.adoptapet.databinding.ItemRequestBinding
import com.cibertec.adoptapet.models.AdoptionRequest

class RequestAdapter(
    private var solicitudes: List<AdoptionRequest>,
    private val onRequestClick: (AdoptionRequest) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>()  {

    inner class RequestViewHolder(
        private val binding: ItemRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(request: AdoptionRequest) {
            binding.txtPetName.text = "Solicitud para ${request.petName}"
            binding.txtApplicant.text = "Codigo: ADP-${request.id}"
            binding.txtDate.text = "Fecha: ${request.date}"
            binding.txtStatus.text = request.status.replace("_", " ")

            val colorEstado = when (request.status.uppercase()) {
                "APROBADA", "FINALIZADA" -> binding.root.context.getColor(com.cibertec.adoptapet.R.color.color_verde)
                "RECHAZADA", "CANCELADA", "NO_ASISTIO" -> binding.root.context.getColor(com.cibertec.adoptapet.R.color.color_rojo)
                else -> binding.root.context.getColor(com.cibertec.adoptapet.R.color.color_naranja)
            }

            binding.txtStatus.setTextColor(colorEstado)

            binding.root.setOnClickListener {
                onRequestClick(request)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(solicitudes[position])
    }

    override fun getItemCount(): Int = solicitudes.size

    fun actualizarLista(nuevaLista: List<AdoptionRequest>) {
        solicitudes = nuevaLista
        notifyDataSetChanged()
    }
}
