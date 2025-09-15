package com.example.travelogue.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.travelogue.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class MarkerInfoDialogFragment : DialogFragment() {

    private var isStarred = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.popup_log, container, false)

        val title = arguments?.getString("title") ?: "Unknown"
        val postedBy = arguments?.getString("postedBy") ?: "Anonymous"
        val place = arguments?.getString("place") ?: "Unknown"
        val description = arguments?.getString("description") ?: ""
        val imageUrl = arguments?.getString("imageUrl")

        view.findViewById<TextView>(R.id.info_title).text = title
        view.findViewById<TextView>(R.id.posted_by).text = "Posted by $postedBy"
        view.findViewById<TextView>(R.id.place_name).text = place
        view.findViewById<TextView>(R.id.info_description).text = description

        val imageView = view.findViewById<ImageView>(R.id.info_image)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(imageView)
        }



        val starIcon = view.findViewById<ImageView>(R.id.star_icon)
        val user = FirebaseAuth.getInstance().currentUser
        val travelogueId = arguments?.getString("travelogueId") ?: return view

        val userFavoritesRef = FirebaseDatabase.getInstance().reference
            .child("user_favorites")
            .child(user?.uid ?: "")

        userFavoritesRef.child(travelogueId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                isStarred = true
                starIcon.setImageResource(R.drawable.ic_star_filled)
            } else {
                isStarred = false
                starIcon.setImageResource(R.drawable.ic_star_outline)
            }
        }
        starIcon.setOnClickListener {
            isStarred = !isStarred
            starIcon.setImageResource(
                if (isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )

            if (isStarred) {
                userFavoritesRef.child(travelogueId).setValue(true)
            } else {
                userFavoritesRef.child(travelogueId).removeValue()
            }
        }

        return view
    }
}

