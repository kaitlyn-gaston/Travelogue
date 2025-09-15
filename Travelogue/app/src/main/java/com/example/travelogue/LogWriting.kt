package com.example.travelogue

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelogue.data.Travelogue
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class LogWriting : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val storage = FirebaseStorage.getInstance()
    private val realtime_db = FirebaseDatabase.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_writing)

        val dbCollection = db.collection("travelogue")
        val log_image = findViewById<ImageView>(R.id.log_photo)

        val imageUriString = intent.getStringExtra("image_uri")
        imageUriString?.let {
            val imageUri = Uri.parse(it)
            log_image.setImageURI(imageUri)
        }

        val storageReference = storage.reference

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val postBtn = findViewById<Button>(R.id.post_log)
        postBtn.setOnClickListener {
            val imageUri = Uri.parse(imageUriString)

            if (imageUri != null) {
                val ref = storageReference.child("images/${UUID.randomUUID()}")

                // Upload image to Firebase Storage
                ref.putFile(imageUri)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            val currentUser = auth.currentUser?.uid
                            val realtime_db_ref = realtime_db.reference
                            val title = findViewById<TextView>(R.id.title_text).text.toString()
                            val description = findViewById<TextView>(R.id.desc_text).text.toString()
                            val loc = findViewById<TextView>(R.id.location_text).text.toString()
                            val log_date = System.currentTimeMillis()

                            // Get current location and upload Firestore document
                            getCurrentLocation { location ->
                                val latitude = location?.latitude ?: 0.0
                                val longitude = location?.longitude ?: 0.0
                                realtime_db_ref.child("users").child(currentUser.toString()).child("username").get().addOnSuccessListener {
                                    val username = it.value.toString()
                                    val newLog = Travelogue(
                                        "${UUID.randomUUID()}",
                                        title,
                                        loc,
                                        log_date,
                                        description,
                                        currentUser.toString(),
                                        username,
                                        imageUrl,
                                        latitude,
                                        longitude
                                    )

                                    // Upload the document to Firestore
                                    dbCollection.add(newLog)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Log has been added", Toast.LENGTH_SHORT).show()
                                            // Start the MainActivity after document is added (important)
                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Failed to add log: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }.addOnFailureListener{
                                    Toast.makeText(this, "Error getting username", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Get current location
    private fun getCurrentLocation(callback: (Location?) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    callback(location)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }
}
