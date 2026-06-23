package com.cibertec.adoptapet

import android.Manifest
import android.os.Bundle
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cibertec.adoptapet.database.AdoptaPetDatabase
import com.cibertec.adoptapet.databinding.ActivityMainBinding
import com.cibertec.adoptapet.fragments.FavoritesFragment
import com.cibertec.adoptapet.fragments.HomeFragment
import com.cibertec.adoptapet.fragments.NotificationsFragment
import com.cibertec.adoptapet.fragments.ProfileFragment
import com.cibertec.adoptapet.fragments.RequestsFragment
import com.cibertec.adoptapet.util.SystemBarUtils
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)
        AdoptaPetDatabase(this).writableDatabase.close()
        solicitarPermisoNotificaciones()
        registrarTokenFirebase()

        cambiarFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> cambiarFragment(HomeFragment())
                R.id.navSolicitudes -> cambiarFragment(RequestsFragment())
                R.id.navFavoritos -> cambiarFragment(FavoritesFragment())
                R.id.navNotificaciones -> cambiarFragment(NotificationsFragment())
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

    fun abrirSolicitudes() {
        binding.bottomNavigation.selectedItemId = R.id.navSolicitudes
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 200)
        }
    }

    private fun registrarTokenFirebase() {
        try {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    getSharedPreferences("adoptapet_session", MODE_PRIVATE)
                        .edit()
                        .putString("fcm_token", token)
                        .apply()
                    Log.d("AdoptaPetFCM", "Token FCM: $token")
                }
                .addOnFailureListener { error ->
                    Log.w("AdoptaPetFCM", "No se pudo obtener el token FCM", error)
                }
        } catch (error: IllegalStateException) {
            Log.w("AdoptaPetFCM", "Firebase aun no esta configurado", error)
        }
    }
}
