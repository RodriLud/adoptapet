package com.cibertec.adoptapet.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.adoptapet.data.FavoritesRepository
import com.cibertec.adoptapet.databinding.ItemPetBinding
import com.cibertec.adoptapet.models.Pet

class PetAdapter(
    private var pets: List<Pet>,
    private val onItemClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    inner class PetViewHolder(
        private val binding: ItemPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {
            binding.imgMascota.setImageResource(pet.imageRes)
            binding.txtNombre.text = pet.name
            binding.txtInfo.text = "${pet.type} • ${pet.age} • ${pet.gender}"
            binding.txtEtiqueta.text = pet.size
            binding.txtEstado.text = "● Disponible"

            pintarFavorito(pet)

            binding.root.setOnClickListener {
                onItemClick(pet)
            }

            binding.txtFavorito.setOnClickListener {
                FavoritesRepository.cambiarFavorito(pet.id)
                pintarFavorito(pet)
            }
        }

        private fun pintarFavorito(pet: Pet) {
            binding.txtFavorito.text =
                if (FavoritesRepository.esFavorito(pet.id)) {
                    "♥"
                } else {
                    "♡"
                }
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