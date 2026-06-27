package com.cibertec.adoptapet.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.adoptapet.adapters.RequestAdapter
import com.cibertec.adoptapet.data.RequestRepository
import com.cibertec.adoptapet.data.SessionManager
import com.cibertec.adoptapet.databinding.FragmentRequestsBinding
import com.cibertec.adoptapet.models.AdoptionRequest
import com.cibertec.adoptapet.views.RequestDetailActivity

class RequestsFragment : Fragment() {
    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var requestAdapter: RequestAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        configurarRecycler()
    }

    override fun onResume() {
        super.onResume()
        cargarSolicitudes()
    }

    private fun configurarRecycler() {
        requestAdapter = RequestAdapter(emptyList()) { solicitud ->
            abrirDetalle(solicitud)
        }
        binding.rvSolicitudes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSolicitudes.adapter = requestAdapter
    }

    private fun cargarSolicitudes() {
        RequestRepository.cargarSolicitudes(requireContext(), sessionManager) { solicitudes, desdeServidor ->
            if (_binding == null) return@cargarSolicitudes
            requestAdapter.actualizarLista(solicitudes)
            actualizarVista(solicitudes)
        }
    }

    private fun actualizarVista(solicitudes: List<AdoptionRequest>) {
        if (solicitudes.isEmpty()) {
            binding.txtSolicitudesVacio.visibility = View.VISIBLE
            binding.rvSolicitudes.visibility = View.GONE
        } else {
            binding.txtSolicitudesVacio.visibility = View.GONE
            binding.rvSolicitudes.visibility = View.VISIBLE
        }
    }

    private fun abrirDetalle(solicitud: AdoptionRequest) {
        val intent = Intent(requireContext(), RequestDetailActivity::class.java)
        intent.putExtra("requestId", solicitud.id)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
