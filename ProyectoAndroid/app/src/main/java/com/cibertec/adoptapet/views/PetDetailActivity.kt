package com.cibertec.adoptapet.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.cibertec.adoptapet.R
import com.cibertec.adoptapet.data.FavoritesRepository
import com.cibertec.adoptapet.data.PetRepository
import com.cibertec.adoptapet.databinding.ActivityPetDetailBinding
import com.cibertec.adoptapet.util.SystemBarUtils

class PetDetailActivity : AppCompatActivity() {

    private var _binding: ActivityPetDetailBinding? = null
    private val binding get() = _binding!!
    private var petId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityPetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemBarUtils.aplicarBarras(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.headerDetalleMascota) { _, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.toolbarDetalle.updatePadding(top = statusBar)
            binding.imgFavoritoDetalle.updatePadding(top = statusBar)
            insets
        }

        petId = intent.getIntExtra("petId", -1)
        configurarToolbar()
        cargarDatosMascota()
    }

    private fun configurarToolbar() {
        binding.toolbarDetalle.setNavigationOnClickListener {
            finish()
        }
    }

    private fun cargarDatosMascota() {
        PetRepository.buscarMascota(this, petId) { pet ->
            if (pet == null) {
                Toast.makeText(this, "Mascota no encontrada", Toast.LENGTH_SHORT).show()
                finish()
                return@buscarMascota
            }

            Glide.with(this)
                .load(pet.imageUrl)
                .placeholder(pet.imageRes)
                .error(R.drawable.kitten1)
                .into(binding.imgDetalleMascota)

            binding.txtDetalleNombre.text = pet.name
            binding.txtDetalleInfo.text = "${pet.type} - ${pet.age} - ${pet.gender}"
            binding.txtDetalleSize.text = "Raza: ${pet.breed.ifBlank { pet.size }}"
            binding.txtDetalleEsterilizado.text =
                if (pet.sterilized) "Esterilizado: Si" else "Esterilizado: No"
            binding.txtDetalleEstado.text = getString(R.string.pet_available)
            binding.txtDetalleDescripcion.text = pet.description

            pintarFavorito(pet.id)

            binding.imgFavoritoDetalle.setOnClickListener {
                FavoritesRepository.cambiarFavorito(pet.id)
                pintarFavorito(pet.id)
            }

            binding.btnSolicitarAdopcion.setOnClickListener {
                val intent = Intent(this, AdoptionFormActivity::class.java)
                intent.putExtra("petId", pet.id)
                startActivity(intent)
            }
        }
    }

    private fun pintarFavorito(petId: Int) {
        binding.imgFavoritoDetalle.setImageResource(
            if (FavoritesRepository.esFavorito(petId)) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_border
            }
        )
    }
}
