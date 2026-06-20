package com.cibertec.adoptapet.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.adoptapet.data.FirebaseRepository
import com.cibertec.adoptapet.databinding.FragmentProfileBinding
import com.cibertec.adoptapet.views.WelcomeActivity
import java.util.Locale

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val repository = FirebaseRepository()

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

        // Obtener datos desde Firestore
        val uid = repository.getCurrentUserUid()
        if (uid != null) {
            repository.getUserData(uid,
                onSuccess = { data ->
                    if (data != null && _binding != null) {
                        actualizarUI(data)
                    }
                },
                onError = { error ->
                    if (context != null) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        binding.btnCerrarSesion.setOnClickListener {
            repository.logout()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun actualizarUI(data: Map<String, Any>) {
        val nombre = data["name"] as? String ?: "Usuario"
        val rol = data["rol"] as? String ?: "ROLE_ADOPTANTE"

        binding.txtNombrePerfil.text = nombre
        binding.txtCorreoPerfil.text = data["email"] as? String ?: ""
        binding.txtInicialPerfil.text = iniciales(nombre)
        
        // Formatear el rol
        binding.txtRolPerfil.text = rol.replace("ROLE_", "")
            .lowercase(Locale.ROOT)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        binding.txtDniPerfil.text = "DNI: ${data["dni"] ?: "-"}"
        binding.txtTelefonoPerfil.text = "Telefono: ${data["fono"] ?: "-"}"
        binding.txtDireccionPerfil.text = "Direccion: ${data["direccion"] ?: "-"}"
    }

    private fun iniciales(nombre: String): String {
        return nombre
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "AP" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
