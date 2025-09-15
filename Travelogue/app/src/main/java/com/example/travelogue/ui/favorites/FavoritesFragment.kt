package com.example.travelogue.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelogue.DetailedLogView
import com.example.travelogue.R
import com.example.travelogue.data.Travelogue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var noFavoritesText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var favoritesAdapter: TravelogueAdapter
    private lateinit var database: DatabaseReference
    private var favoritesList = mutableListOf<Travelogue>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_favorites)
        noFavoritesText = view.findViewById(R.id.text_no_favorites)
        progressBar = view.findViewById(R.id.progress_bar)

        setupRecyclerView()
        setupFirebaseListener()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = TravelogueAdapter(favoritesList) { travelogue ->
            val intent = Intent(requireContext(), DetailedLogView::class.java)
            intent.putExtra("travelogue", travelogue)
            startActivity(intent)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoritesAdapter
        }
    }

    private fun setupFirebaseListener() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            showNoFavorites()
            return
        }

        showLoading()
        database = FirebaseDatabase.getInstance().reference
            .child("user_favorites")
            .child(currentUser.uid)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoritesList.clear()
                for (favoriteSnapshot in snapshot.children) {
                    val travelogueId = favoriteSnapshot.key
                    // Fetch the actual travelogue data
                    fetchTravelogueDetails(travelogueId)
                }
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                showNoFavorites()
            }
        })
    }

    private fun fetchTravelogueDetails(travelogueId: String?) {
        if (travelogueId == null) return

        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        firestore.collection("travelogue")
            .document(travelogueId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val travelogue = document.toObject(Travelogue::class.java)
                    travelogue?.let {
                        it.id = document.id  // 🔐 Ensure ID is attached
                        favoritesList.add(it)
                        favoritesAdapter.notifyDataSetChanged()
                        updateUI()
                    }
                }
            }
            .addOnFailureListener {
                // Optionally log or show an error
            }
    }


    private fun updateUI() {
        hideLoading()
        if (favoritesList.isEmpty()) {
            showNoFavorites()
        } else {
            showFavorites()
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        noFavoritesText.visibility = View.GONE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showNoFavorites() {
        recyclerView.visibility = View.GONE
        noFavoritesText.visibility = View.VISIBLE
    }

    private fun showFavorites() {
        recyclerView.visibility = View.VISIBLE
        noFavoritesText.visibility = View.GONE
    }
} 