package com.example.travelogue.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.travelogue.R
import com.example.travelogue.data.Travelogue
import com.example.travelogue.ui.home.HomeFragment
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TravelogueAdapter(
    options: FirestoreRecyclerOptions<Travelogue>,
    private val onItemClick: (Travelogue) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : FirestoreRecyclerAdapter<Travelogue, TravelogueAdapter.ViewHolder>(options) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.text_title)
        val dateText: TextView = view.findViewById(R.id.text_date)
        val locationText: TextView = view.findViewById(R.id.text_location)
        val deleteButton: ImageView = view.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travelogue_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, travelogue: Travelogue) {
        holder.titleText.text = travelogue.title
        holder.locationText.text = travelogue.location
        holder.dateText.text = formatDate(travelogue.date)
        holder.deleteButton.setImageResource(R.drawable.baseline_block_flipped_24)

        // Item click listener
        holder.itemView.setOnClickListener {
            onItemClick(travelogue)
        }

        // Delete button listener
        holder.deleteButton.setOnClickListener {
            val docId = snapshots.getSnapshot(position).id
            onDeleteClick(docId)
        }
    }

    // Format the timestamp to a readable date
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
