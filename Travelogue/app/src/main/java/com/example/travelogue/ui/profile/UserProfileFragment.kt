package com.example.travelogue.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.travelogue.DetailedLogView
import com.example.travelogue.R
import com.example.travelogue.data.Travelogue
import com.example.travelogue.databinding.FragmentUserProfileBinding
import com.example.travelogue.ui.profile.TravelogueAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var adapter: TravelogueAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerTravelogues.itemAnimator = null
        binding.recyclerTravelogues.layoutManager = LinearLayoutManager(requireContext())
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadUserData()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            loadTravelogues()
        }

    }

    private fun setupUI() {
        binding.btnChangePhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.recyclerTravelogues.layoutManager = LinearLayoutManager(context)
        
        // Set default profile image
        Glide.with(this)
            .load(R.drawable.default_avatar)
            .circleCrop()
            .into(binding.profileImage)
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        binding.progressBar.visibility = View.VISIBLE

        database.reference.child("users").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    if (snapshot.exists()) {
                        binding.editUsername.setText(snapshot.child("username").value?.toString() ?: "")
                        binding.editBio.setText(snapshot.child("bio").value?.toString() ?: "")
                        val photoUrl = snapshot.child("photoUrl").value?.toString()
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this@UserProfileFragment)
                                .load(photoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .into(binding.profileImage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveProfile() {
        val currentUser = auth.currentUser ?: return
        val username = binding.editUsername.text.toString().trim()
        val bio = binding.editBio.text.toString().trim()

        if (username.isEmpty()) {
            binding.editUsername.error = "Username cannot be empty"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        val userUpdates = mapOf(
            "username" to username,
            "bio" to bio
        )

        database.reference.child("users").child(currentUser.uid)
            .updateChildren(userUpdates)
            .addOnSuccessListener {
                updateUsernameInTravelogues(currentUser.uid, username)
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUsernameInTravelogues(userId: String, newUsername: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("travelogue")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    db.collection("travelogue").document(document.id)
                        .update("userName", newUsername)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpdate", "Error updating usernames in travelogue: ${e.message}")
            }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data ?: return
            uploadProfileImage(imageUri)
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val currentUser = auth.currentUser ?: return
        binding.progressBar.visibility = View.VISIBLE

        // Show selected image immediately while uploading
        Glide.with(this)
            .load(imageUri)
            .circleCrop()
            .into(binding.profileImage)

        val imageRef = storage.reference
            .child("profile_images")
            .child(currentUser.uid)
            .child(UUID.randomUUID().toString())

        imageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString()
                    database.reference.child("users")
                        .child(currentUser.uid)
                        .child("photoUrl")
                        .setValue(downloadUrl)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile photo updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to update profile photo", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    // Reload previous image
                    loadUserData()
                }
            }
    }

    private fun loadTravelogues() {
        val currentUser = auth.currentUser ?: return

        binding.progressBar.visibility = View.VISIBLE

        // Query to get travelogues for the current user
        val query = FirebaseFirestore.getInstance()
            .collection("travelogue")
            .whereEqualTo("userId", currentUser.uid)

        val options = FirestoreRecyclerOptions.Builder<Travelogue>()
            .setQuery(query, Travelogue::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        adapter = TravelogueAdapter(options,
            onItemClick = { travelogue ->
                val intent = Intent(requireContext(), DetailedLogView::class.java)
                intent.putExtra("travelogue", travelogue)
                startActivity(intent)
            },
            onDeleteClick = { docId ->
                FirebaseFirestore.getInstance()
                    .collection("travelogue")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Travelogue deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }
            }
        )

        // Set adapter to the RecyclerView
        binding.recyclerTravelogues.post {
            if (isAdded && _binding != null) {
                binding.recyclerTravelogues.adapter = adapter
            }
        }



        // Hide progress bar after data is loaded (done after adapter is set up)
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun resetAdapter() {
        if (::adapter.isInitialized) {
            binding.recyclerTravelogues.adapter = null
        }
    }


} 