package com.cibertec.adoptapet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.adoptapet.MainActivity
import com.cibertec.adoptapet.adapters.NotificationAdapter
import com.cibertec.adoptapet.data.NotificationRepository
import com.cibertec.adoptapet.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationAdapter = NotificationAdapter(emptyList()) { notification ->
            NotificationRepository.marcarLeida(requireContext(), notification.id)
            (activity as? MainActivity)?.abrirSolicitudes()
            actualizarVista()
        }

        binding.rvNotificaciones.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotificaciones.adapter = notificationAdapter

        actualizarVista()
    }

    override fun onResume() {
        super.onResume()
        if (::notificationAdapter.isInitialized) {
            actualizarVista()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun actualizarVista() {
        val notificaciones = NotificationRepository.listar(requireContext())
        notificationAdapter.actualizarLista(notificaciones)

        if (notificaciones.isEmpty()) {
            binding.txtNotificacionesVacio.visibility = View.VISIBLE
            binding.rvNotificaciones.visibility = View.GONE
        } else {
            binding.txtNotificacionesVacio.visibility = View.GONE
            binding.rvNotificaciones.visibility = View.VISIBLE
        }
    }
}
