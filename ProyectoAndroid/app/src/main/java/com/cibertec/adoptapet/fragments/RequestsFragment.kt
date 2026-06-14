package com.cibertec.adoptapet.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.cibertec.adoptapet.adapters.RequestAdapter
import com.cibertec.adoptapet.data.RequestRepository
import com.cibertec.adoptapet.databinding.FragmentRequestsBinding

class RequestsFragment : Fragment() {
    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var requestAdapter: RequestAdapter

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
        //add code here for the new view request information
        actualizarVista()

    }


    private fun actualizarVista() {
        val solicitudes = RequestRepository.solicitudes

        if (solicitudes.isEmpty()) {
            binding.txtSolicitudesVacio.visibility = View.VISIBLE
            binding.rvSolicitudes.visibility = View.GONE
        } else {
            binding.txtSolicitudesVacio.visibility = View.GONE
            binding.rvSolicitudes.visibility = View.VISIBLE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}