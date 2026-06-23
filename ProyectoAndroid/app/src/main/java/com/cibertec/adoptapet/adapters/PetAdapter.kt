package com.cibertec.adoptapet.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cibertec.adoptapet.R
import com.cibertec.adoptapet.data.FavoritesRepository
import com.cibertec.adoptapet.databinding.ItemPetBinding
import com.cibertec.adoptapet.models.Pet

class PetAdapter(
    private var pets: List<Pet>,
    private val onItemClick: (Pet) -> Unit,
    private val onFavoritoChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    inner class PetViewHolder(
        private val binding: ItemPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {
            Glide.with(binding.imgMascota.context)
                .load(pet.imageUrl)
                .placeholder(pet.imageRes)
                .error(R.drawable.kitten1)
                .into(binding.imgMascota)

            binding.txtNombre.text = pet.name
            binding.txtInfo.text = "${pet.type} - ${pet.age} - ${pet.gender}"
            binding.txtEtiqueta.text = pet.size
            binding.txtEstado.text = "Disponible"

            pintarFavorito(pet)

            binding.root.setOnClickListener {
                onItemClick(pet)
            }

            binding.imgFavorito.setOnClickListener {
                FavoritesRepository.cambiarFavorito(pet.id)
                pintarFavorito(pet)
                onFavoritoChanged?.invoke()
            }
        }

        private fun pintarFavorito(pet: Pet) {
            binding.imgFavorito.setImageResource(
                if (FavoritesRepository.esFavorito(pet.id)) {
                    R.drawable.ic_favorite_filled
                } else {
                    R.drawable.ic_favorite_border
                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(pets[position])
    }

    override fun getItemCount(): Int = pets.size

    fun actualizarLista(nuevaLista: List<Pet>) {
        pets = nuevaLista
        notifyDataSetChanged()
    }
}
