package com.cibertec.adoptapet

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.cibertec.adoptapet.databinding.ActivityMainBinding
import com.cibertec.adoptapet.fragments.FavoritesFragment
import com.cibertec.adoptapet.fragments.HomeFragment
import com.cibertec.adoptapet.fragments.ProfileFragment
import com.cibertec.adoptapet.fragments.RequestsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cambiarFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> cambiarFragment(HomeFragment())
                R.id.navSolicitudes -> cambiarFragment(RequestsFragment())
                R.id.navFavoritos -> cambiarFragment(FavoritesFragment())
                R.id.navNotificaciones -> cambiarFragment(HomeFragment())
                R.id.navPerfil -> cambiarFragment(ProfileFragment())
            }
            true
        }

        val abrirSolicitudes = intent.getBooleanExtra("abrirSolicitudes", false)

        if (abrirSolicitudes) {
            binding.bottomNavigation.selectedItemId = R.id.navSolicitudes
        } else {
            cambiarFragment(HomeFragment())
        }

    }

    private fun cambiarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmentos, fragment)
            .commit()
    }
}