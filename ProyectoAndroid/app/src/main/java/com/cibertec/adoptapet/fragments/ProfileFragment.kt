package com.cibertec.adoptapet.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cibertec.adoptapet.data.SessionManager
import com.cibertec.adoptapet.databinding.FragmentProfileBinding
import com.cibertec.adoptapet.views.WelcomeActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val nombre = sessionManager.obtenerNombre()
        binding.txtNombrePerfil.text = nombre
        binding.txtCorreoPerfil.text = sessionManager.obtenerEmail()
        binding.txtInicialPerfil.text = iniciales(nombre)
        binding.txtRolPerfil.text = sessionManager.obtenerRol().replace("ROLE_", "").lowercase()
            .replaceFirstChar { it.uppercase() }
        binding.txtDniPerfil.text = "DNI: ${valorPerfil(sessionManager.obtenerDni())}"
        binding.txtTelefonoPerfil.text = "Telefono: ${valorPerfil(sessionManager.obtenerTelefono())}"
        binding.txtDireccionPerfil.text = "Direccion: ${valorPerfil(sessionManager.obtenerDireccion())}"

        binding.btnCerrarSesion.setOnClickListener {
            sessionManager.cerrarSesion()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun iniciales(nombre: String): String {
        return nombre
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "AP" }
    }

    private fun valorPerfil(valor: String): String {
        return valor.ifBlank { "-" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
