package com.example.travelogue.ui.favorites

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.travelogue.DetailedLogView
import com.example.travelogue.LogWriting
import com.example.travelogue.R
import com.example.travelogue.data.Travelogue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class TravelogueAdapter(
    private val travelogues: List<Travelogue>,
    private val onItemClick: (Travelogue) -> Unit
) : RecyclerView.Adapter<TravelogueAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.text_title)
        val dateText: TextView = view.findViewById(R.id.text_date)
        val locationText: TextView = view.findViewById(R.id.text_location)
        val favoriteButton: ImageView = view.findViewById(R.id.button_favorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travelogue, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val travelogue: Travelogue = travelogues[position]
        
        holder.titleText.text = travelogue.title
        holder.locationText.text = travelogue.location
        holder.dateText.text = formatDate(travelogue.date)
        
        // Set favorite icon to filled since these are all favorites
        holder.favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
        
        holder.itemView.setOnClickListener {
            onItemClick(travelogue)
        }

        holder.favoriteButton.setOnClickListener {
            removeFavorite(travelogue.id)
        }
    }

    override fun getItemCount(): Int = travelogues.size

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun removeFavorite(travelogueId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        
        FirebaseDatabase.getInstance().reference
            .child("user_favorites")
            .child(currentUser.uid)
            .child(travelogueId)
            .removeValue()
    }
} 