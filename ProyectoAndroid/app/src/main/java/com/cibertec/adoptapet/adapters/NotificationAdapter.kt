package com.cibertec.adoptapet.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.adoptapet.databinding.ItemNotificationBinding
import com.cibertec.adoptapet.models.AppNotification

class NotificationAdapter(
    private var notifications: List<AppNotification>,
    private val onItemClick: (AppNotification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: AppNotification) {
            binding.txtTituloNotificacion.text = notification.title
            binding.txtCuerpoNotificacion.text = notification.body
            binding.txtFechaNotificacion.text = notification.date

            binding.root.alpha = if (notification.read) 0.75f else 1f

            binding.root.setOnClickListener {
                onItemClick(notification)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    fun actualizarLista(nuevaLista: List<AppNotification>) {
        notifications = nuevaLista
        notifyDataSetChanged()
    }
}
